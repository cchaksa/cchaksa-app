package com.chukchukhaksa.mobile.remote.portalscrape

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReferenceRankingRawCollectorTest {

  private val portal = FakeSuwonPortal()

  private suspend fun scrape(): PortalScrapeRawResult = withContext(Dispatchers.Default) {
    SuwonPortalScraper(clientFactory = portal::client).scrape("24020044", "secret")
  }

  private fun PortalScrapeRawResult.responses(): List<JsonObject> =
    referenceRankingRaw!!["responses"]!!.jsonArray.map { it.jsonObject }

  private fun List<JsonObject>.find(source: String, year: Int? = null, semester: Int? = null): JsonObject = single {
    it.text("source") == source && it["year"] == JsonPrimitive(year) && it["semester"] == JsonPrimitive(semester)
  }

  @Test
  fun `학기별 성적·수강 원문과 학적 조회를 서버 계약 순서대로 모은다`() = runTest {
    val result = scrape()

    assertEquals(1, result.referenceRankingRaw!!["schemaVersion"]?.jsonPrimitive?.content?.toInt())
    assertEquals(
      listOf(
        "GRADE_SUMMARY" to null, "COURSE_TERMS" to null,
        "GRADE_DETAIL" to "2024-20", "COURSE_DETAIL" to "2024-20",
        "GRADE_DETAIL" to "2025-10", "COURSE_DETAIL" to "2025-10",
        "REGISTRATION" to null, "MAJOR_HISTORY" to null, "MAJOR_ASSIGNMENT" to "2025-10",
      ),
      result.responses().map { it.text("source") to it.term() },
    )
    // 기본 학사 자료와 별도로 보내며, 기존 원문 구조는 바꾸지 않는다.
    assertEquals(listOf("student", "semesters", "academicRecords", "designatedCourses"), result.toJsonObject().keys.toList())
  }

  @Test
  fun `이미 조회한 성적·수강 응답은 다시 조회하지 않고 목록에 없는 이전 정규학기만 추가로 조회한다`() = runTest {
    scrape()

    val gradeDetailTerms = portal.requestsTo(CREDIT_DETAIL_PATH).map { it.jsonBody().term("cretGainYear", "cretSmrCd") }
    assertEquals(listOf("2025-10", "2024-20"), gradeDetailTerms)
    val courseDetailTerms = portal.requestsTo(COURSE_DETAIL_PATH).map { it.jsonBody().term("subjtEstbYear", "subjtEstbSmrCd") }
    assertEquals(listOf("2025-10", "2024-20"), courseDetailTerms)
    assertEquals(1, portal.requestsTo(GRADE_SUMMARY_PATH).size)
    assertEquals(1, portal.requestsTo(COURSE_SEMESTERS_PATH).size)
    // 새로 조회하는 학기는 학번과 함께 문자열로 보낸다.
    assertEquals(
      buildJsonObject { put("sno", "24020044"); put("cretGainYear", "2024"); put("cretSmrCd", "20") },
      portal.requestsTo(CREDIT_DETAIL_PATH).last().jsonBody(),
    )
  }

  @Test
  fun `성적·수강 응답은 받은 텍스트를 그대로 보낸다`() = runTest {
    val summaryText = """{ "listSmrCretSumTabYearSmr" : [ {"sno":"24020044","cretGainYear":"2025","cretSmrCd":"10",""" +
      """"gainPoint":4.0,"applAvmk":"4.50","oriApplAvmk":null} ], "selectSmrCretSumTabSjTotal":{"gainPoint":"4"} }"""
    portal.gradeSummaryText = summaryText

    val responses = scrape().responses()

    val summary = responses.find("GRADE_SUMMARY")
    assertEquals(summaryText, summary.text("responseBody"))
    assertEquals("SUCCESS", summary.text("status"))
    assertEquals(JsonPrimitive(200), summary["httpStatus"])
    assertEquals(JsonNull, summary["errorCode"])
    val gradeDetail = responses.find("GRADE_DETAIL", 2025, 10)
    assertEquals(
      buildJsonObject { put("listSmrCretSumTabSubjt", FakeSuwonPortal.defaultCredits()) }.toString(),
      gradeDetail.text("responseBody"),
    )
  }

  @Test
  fun `학적 응답은 대상 필드와 허용 필드만 남긴다`() = runTest {
    val responses = scrape().responses()

    assertEquals(
      """{"listTutnTab":[{"regYearSmr":"2025-1학기","studGrde":"2"}]}""",
      responses.find("REGISTRATION").text("responseBody"),
    )
    // 숫자 타입과 null은 그대로 둔다.
    assertEquals(
      """{"listMjorChngInfoTab":[{"mjorChngSeq":1,"mjorChngYearSmr":"2025-1학기","mjorChngNm":"주전공배정",""" +
        """"achgMjorNm":"정보통신","mjorChngCancDt":null}]}""",
      responses.find("MAJOR_HISTORY").text("responseBody"),
    )
    assertEquals(
      """{"mjorChngAppl":{"sno":"24020044","mjorChngCd":"11","applYear":"2025","applSmrCd":"10",""" +
        """"alotMjorCd":"2000516","applStatCd":"30","mjorStatCd":"승인"}}""",
      responses.find("MAJOR_ASSIGNMENT", 2025, 10).text("responseBody"),
    )
  }

  @Test
  fun `전공배정은 성적 학기가 아니라 취소되지 않은 주전공배정 이력의 학기로 조회한다`() = runTest {
    portal.majorHistoryResponse = """{"listMjorChngInfoTab":[""" +
      """{"mjorChngYearSmr":"2024-1학기","mjorChngNm":"주전공배정","achgMjorNm":"정보통신","mjorChngCancDt":null},""" +
      """{"mjorChngYearSmr":"2024-2학기","mjorChngNm":"주전공배정","achgMjorNm":"정보보호","mjorChngCancDt":"20240901"},""" +
      """{"mjorChngYearSmr":"2025-1학기","mjorChngNm":"전과","achgMjorNm":"컴퓨터SW","mjorChngCancDt":""}]}"""
    portal.majorAssignments = emptyMap()

    val responses = scrape().responses()

    val requests = portal.requestsTo(MAJOR_ASSIGNMENT_PATH).map { it.jsonBody() }
    assertEquals(
      listOf(buildJsonObject { put("sno", "24020044"); put("mjorChngCd", "11"); put("applYear", "2024"); put("applSmrCd", "10") }),
      requests,
    )
    // 결과가 없는 전공배정은 빈 결과다.
    val assignment = responses.find("MAJOR_ASSIGNMENT", 2024, 10)
    assertEquals("EMPTY", assignment.text("status"))
    assertEquals("""{"mjorChngAppl":null}""", assignment.text("responseBody"))
  }

  @Test
  fun `빈 목록은 EMPTY, 조회 실패는 FAILED로 구분한다`() = runTest {
    val responses = scrape().responses()

    // 가짜 포털은 성적이 없는 학기의 성적 상세에 500을 준다.
    assertEquals(failed(500, "HTTP_ERROR"), responses.find("GRADE_DETAIL", 2024, 20).withoutSource())
    val emptyCourse = responses.find("COURSE_DETAIL", 2024, 20)
    assertEquals("EMPTY", emptyCourse.text("status"))
    assertEquals("""{"listAtlecApplDtaiTabSubjt":[]}""", emptyCourse.text("responseBody"))
  }

  @Test
  fun `계절학기는 상세 조회 대상에서 빼고 이전 학기도 정규학기로 계산한다`() = runTest {
    portal.courseSemesters = buildJsonArray {
      add(buildJsonObject { put("subjtEstbYear", "2025"); put("subjtEstbSmrCd", "15") })
      add(buildJsonObject { put("subjtEstbYear", "2025"); put("subjtEstbSmrCd", "20") })
    }
    portal.coursesBySemester = mapOf("2025-15" to JsonArray(emptyList()), "2025-20" to JsonArray(emptyList()))

    val responses = scrape().responses()

    assertEquals(
      listOf("2024-20", "2025-10", "2025-20"),
      responses.filter { it.text("source") == "COURSE_DETAIL" }.map { it.term() },
    )
    assertTrue(responses.none { it["semester"] == JsonPrimitive(15) })
  }

  @Test
  fun `학적 조회가 실패해도 기본 학사 자료와 나머지 원본은 그대로 만든다`() = runTest {
    portal.registrationResponse = """{"errorMessage":"로그인 하지 않았거나, 세션이 종료되었습니다.","errorCode":"-998"}"""
    portal.majorHistoryResponse = "<html>로그인</html>"

    val result = scrape()

    val responses = result.responses()
    assertEquals(failed(200, "SESSION_EXPIRED"), responses.find("REGISTRATION").withoutSource())
    assertEquals(failed(200, "INVALID_RESPONSE"), responses.find("MAJOR_HISTORY").withoutSource())
    assertTrue(responses.none { it.text("source") == "MAJOR_ASSIGNMENT" })
    assertTrue(portal.requestsTo(MAJOR_ASSIGNMENT_PATH).isEmpty())
    assertEquals(1, result.semesters.size)
  }

  @Test
  fun `일부 학기 조회가 실패한 결과는 올리지 않으므로 원본을 모으지 않는다`() = runTest {
    portal.gradeSummary = buildJsonObject {
      put(
        "listSmrCretSumTabYearSmr",
        buildJsonArray {
          add(buildJsonObject { put("cretGainYear", "2025"); put("cretSmrCd", "10") })
          add(buildJsonObject { put("cretGainYear", "2024"); put("cretSmrCd", "20") })
        },
      )
      put("selectSmrCretSumTabSjTotal", buildJsonObject { put("gainPoint", "4") })
    }
    val progress = mutableListOf<PortalScrapeProgress>()

    val result = withContext(Dispatchers.Default) {
      SuwonPortalScraper(clientFactory = portal::client).scrape("24020044", "secret") { progress += it }
    }

    assertTrue(result.warnings.isNotEmpty())
    assertNull(result.referenceRankingRaw)
    assertTrue(portal.requestsTo(REGISTRATION_PATH).isEmpty())
    assertTrue(progress.any { it is PortalScrapeProgress.Skipped && it.step == PortalScrapeStep.REFERENCE_RANKING })
  }

  @Test
  fun `서버 검증에 맞지 않는 응답은 그 응답만 FAILED로 보낸다`() {
    val term = RegularTerm(2025, 10)
    fun entryOf(source: ReferenceRankingSource, body: String, entryTerm: RegularTerm? = null) =
      entry(source, entryTerm, PortalAcademicExchange("/path", JsonObject(emptyMap()), 200, body, null), "24020044").withoutSource()

    // 다른 학번
    assertEquals(
      failed(200, "INVALID_RESPONSE"),
      entryOf(ReferenceRankingSource.GRADE_SUMMARY, """{"listSmrCretSumTabYearSmr":[{"sno":"99999999"}]}"""),
    )
    // 숫자 학번
    assertEquals(
      failed(200, "INVALID_RESPONSE"),
      entryOf(ReferenceRankingSource.GRADE_SUMMARY, """{"listSmrCretSumTabYearSmr":[],"total":{"sno":24020044}}"""),
    )
    // 조회 학기와 다른 행
    assertEquals(
      failed(200, "INVALID_RESPONSE"),
      entryOf(ReferenceRankingSource.GRADE_DETAIL, """{"listSmrCretSumTabSubjt":[{"cretGainYear":"2025","cretSmrCd":"20"}]}""", term),
    )
    // 대상 필드 누락·배열이 아님
    assertEquals(failed(200, "INVALID_RESPONSE"), entryOf(ReferenceRankingSource.COURSE_TERMS, """{"other":[]}"""))
    assertEquals(failed(200, "INVALID_RESPONSE"), entryOf(ReferenceRankingSource.COURSE_TERMS, """{"listAtlecApplDtaiTabYearSmr":null}"""))
    assertEquals(failed(200, "INVALID_RESPONSE"), entryOf(ReferenceRankingSource.MAJOR_ASSIGNMENT, """{"other":{}}""", term))
    // 100행 초과
    val rows = (1..101).joinToString(",") { "{}" }
    assertEquals(failed(200, "INVALID_RESPONSE"), entryOf(ReferenceRankingSource.COURSE_TERMS, """{"listAtlecApplDtaiTabYearSmr":[$rows]}"""))
    // 200,000자 초과
    val long = "x".repeat(200_000)
    assertEquals(
      failed(200, "INVALID_RESPONSE"),
      entryOf(ReferenceRankingSource.COURSE_TERMS, """{"listAtlecApplDtaiTabYearSmr":[{"memo":"$long"}]}"""),
    )
    // 다른 학기의 전공배정 결과
    assertEquals(
      failed(200, "INVALID_RESPONSE"),
      entryOf(ReferenceRankingSource.MAJOR_ASSIGNMENT, """{"mjorChngAppl":{"applYear":"2024","applSmrCd":"10"}}""", term),
    )
    // 빈 전공배정 객체는 빈 결과다.
    assertEquals("EMPTY", entryOf(ReferenceRankingSource.MAJOR_ASSIGNMENT, """{"mjorChngAppl":{}}""", term).text("status"))
    // 정수 학기 코드도 같은 학기로 본다.
    assertEquals(
      "SUCCESS",
      entryOf(ReferenceRankingSource.GRADE_DETAIL, """{"listSmrCretSumTabSubjt":[{"cretGainYear":2025,"cretSmrCd":10}]}""", term)
        .text("status"),
    )
  }

  @Test
  fun `응답을 받지 못한 조회는 HTTP 상태 없이 NETWORK_ERROR로 보낸다`() {
    val exchange = PortalAcademicExchange("/path", JsonObject(emptyMap()), null, null, IOException("reset"))

    val entry = entry(ReferenceRankingSource.GRADE_DETAIL, RegularTerm(2025, 10), exchange, "24020044")

    assertEquals(
      buildJsonObject {
        put("source", "GRADE_DETAIL")
        put("year", 2025)
        put("semester", 10)
        put("status", "FAILED")
        put("httpStatus", JsonNull)
        put("responseBody", JsonNull)
        put("errorCode", "NETWORK_ERROR")
      },
      entry,
    )
  }

  @Test
  fun `학기 표기와 이전 정규학기`() {
    assertEquals(RegularTerm(2025, 20), RegularTerm.ofLabel(" 2025-2학기 "))
    assertNull(RegularTerm.ofLabel("2025-여름학기"))
    assertEquals(RegularTerm(2025, 20), RegularTerm(2026, 10).previous())
    assertEquals(RegularTerm(2026, 10), RegularTerm(2026, 20).previous())
  }

  private fun failed(httpStatus: Int, errorCode: String) = buildJsonObject {
    put("status", "FAILED")
    put("httpStatus", httpStatus)
    put("responseBody", JsonNull)
    put("errorCode", errorCode)
  }

  private fun JsonObject.withoutSource() = JsonObject(filterKeys { it !in setOf("source", "year", "semester") })

  private fun JsonObject.text(key: String): String? = (this[key] as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content

  private fun JsonObject.term(): String? = this["year"]?.takeUnless { it is JsonNull }?.let { "${it.jsonPrimitive.content}-${this["semester"]!!.jsonPrimitive.content}" }

  private fun JsonObject.term(yearKey: String, semesterKey: String) = "${text(yearKey)}-${text(semesterKey)}"
}
