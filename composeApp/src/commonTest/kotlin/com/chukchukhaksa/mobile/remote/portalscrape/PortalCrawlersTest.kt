package com.chukchukhaksa.mobile.remote.portalscrape

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeErrorCode
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortalCrawlersTest {

  private val requests = mutableListOf<HttpRequestData>()

  private fun crawlers(status: HttpStatusCode = HttpStatusCode.OK, body: (HttpRequestData) -> String): HttpPortalCrawlers {
    val engine = MockEngine { request ->
      requests += request
      respond(body(request), status, headersOf(HttpHeaders.ContentType, "application/json;charset=UTF-8"))
    }
    return HttpPortalCrawlers(HttpClient(engine) { configurePortalClient() })
  }

  // studentCrawler.spec.ts
  @Test
  fun `입학 구분과 외국어 인증 상태를 원본 그대로 매핑한다`() = runTest {
    val studentInfo = FakeSuwonPortal.defaultStudentInfo().let { info ->
      buildJsonObject {
        info.forEach { (key, value) -> put(key, value) }
        put("enscDvcd", "2")
      }
    }

    val student = crawlers { buildJsonObject { put("studentInfo", studentInfo) }.toString() }.scrapeStudent("24020044")

    assertEquals(JsonPrimitive("2"), student["enscDvcd"])
    assertEquals(JsonPrimitive("미통과"), student["flangPassGb"])
    // 스크래퍼가 고르는 필드만 담고, 이수 학기는 parseInt 결과(숫자)로 바꾼다.
    assertFalse(student.containsKey("studIidno"))
    assertEquals(JsonPrimitive(3), student["facSmrCnt"])
    assertEquals(
      listOf("sno", "studNm", "univCd", "univNm", "dpmjCd", "dpmjNm", "mjorCd", "mjorNm", "enscYear", "enscSmrCd", "scrgStatNm", "studGrde", "enscDvcd", "facSmrCnt", "flangPassGb"),
      student.keys.toList(),
    )
  }

  @Test
  fun `외국어 인증 상태가 누락되면 필드를 만들지 않는다`() = runTest {
    val studentInfo = buildJsonObject {
      FakeSuwonPortal.defaultStudentInfo().filterKeys { it != "flangPassGb" }.forEach { (key, value) -> put(key, value) }
    }

    val student = crawlers { buildJsonObject { put("studentInfo", studentInfo) }.toString() }.scrapeStudent("24020044")

    assertFalse(student.containsKey("flangPassGb"))
  }

  @Test
  fun `이수 학기 수는 parseInt처럼 해석하고 실패하면 0이다`() {
    assertEquals(4, jsParseIntOrZero(JsonPrimitive(" 4학기")))
    assertEquals(3, jsParseIntOrZero(JsonPrimitive(3.7)))
    assertEquals(0, jsParseIntOrZero(JsonPrimitive("")))
    assertEquals(0, jsParseIntOrZero(null))
  }

  @Test
  fun `세션이 없다는 학사 응답은 원인을 담은 오류로 바꾼다`() = runTest {
    val error = assertFailsWith<PortalScrapeException> {
      crawlers { """{"errorMessage":"로그인 하지 않았거나, 세션이 종료되었습니다.","errorCode":"-998"}""" }.scrapeStudent("24020044")
    }

    assertEquals(PortalScrapeErrorCode.BUSINESS_RULE_VIOLATION, error.errorCode)
    assertTrue("errorCode=-998" in error.message)
  }

  // designatedCourseCrawler.spec.ts
  @Test
  fun `지정과목 API에 학번과 조직분류코드 20을 전달하고 precpSbjtList의 원본 필드를 보존한다`() = runTest {
    val courses = buildJsonArray {
      add(buildJsonObject { put("subjtCd", "SUBJ001"); put("point", 3); put("extraPortalField", "원본 추가 필드") })
      add(buildJsonObject { put("subjtCd", "SUBJ002"); put("point", 3) })
    }

    val result = crawlers { buildJsonObject { put("precpSbjtList", courses) }.toString() }.scrapeDesignatedCourses("24020044")

    assertEquals(courses, result)
    assertEquals(1, requests.size)
    assertEquals("https://info.suwon.ac.kr/precpSbjt/listPrecpSbjt.do", requests[0].url.toString())
    assertEquals(buildJsonObject { put("sno", "24020044"); put("orgClsCd", "20") }, requests[0].jsonBody())
    assertEquals("application/json; charset=UTF-8", requests[0].body.contentType.toString())
  }

  @Test
  fun `학부 코드로 조회한 명시적인 precpSbjtList 빈 배열은 정상 결과다`() = runTest {
    assertEquals(JsonArray(emptyList()), crawlers { """{"precpSbjtList":[]}""" }.scrapeDesignatedCourses("24020044"))
  }

  @Test
  fun `잘못된 지정과목 응답 형식은 재시도 불가능한 형식 오류다`() = runTest {
    val invalidResponses = listOf("{}", """{"listPrecpSbjt":[]}""", """{"precpSbjtList":null}""", """{"precpSbjtList":{}}""",
      """{"precpSbjtList":"[]"}""", """{"precpSbjtList":0}""", """{"precpSbjtList":false}""", "null", "[]", "\"응답 원문\"")

    invalidResponses.forEach { response ->
      val error = assertFailsWith<PortalScrapeException>(response) { crawlers { response }.scrapeDesignatedCourses("24020044") }
      assertEquals(PortalScrapeErrorCode.PORTAL_RESPONSE_SCHEMA_MISMATCH, error.errorCode, response)
      assertFalse(error.retryable)
      assertEquals("지정과목 응답의 precpSbjtList가 배열이 아닙니다.", error.message)
    }
  }

  @Test
  fun `JSON 구문 오류는 응답 원문 없이 형식 오류로 전달한다`() = runTest {
    val error = assertFailsWith<PortalScrapeException> { crawlers { "{비공개 응답 원문" }.scrapeDesignatedCourses("24020044") }

    assertEquals(PortalScrapeErrorCode.PORTAL_RESPONSE_SCHEMA_MISMATCH, error.errorCode)
    assertEquals("지정과목 응답을 JSON으로 해석할 수 없습니다.", error.message)
  }

  @Test
  fun `5xx 응답은 재시도 가능한 일시 오류이고 4xx는 재시도 불가 오류다`() = runTest {
    val unavailable = assertFailsWith<PortalScrapeException> {
      crawlers(HttpStatusCode.ServiceUnavailable) { "{}" }.scrapeDesignatedCourses("24020044")
    }
    val badRequest = assertFailsWith<PortalScrapeException> {
      crawlers(HttpStatusCode.BadRequest) { "{}" }.scrapeDesignatedCourses("24020044")
    }

    assertEquals(PortalScrapeErrorCode.PORTAL_TEMPORARY_UNAVAILABLE, unavailable.errorCode)
    assertTrue(unavailable.retryable)
    assertEquals(PortalScrapeErrorCode.UNKNOWN_NON_RETRYABLE, badRequest.errorCode)
    assertFalse(badRequest.retryable)
  }

  // creditCrawler.ts
  @Test
  fun `성적 합계가 없으면 신입생 기본 성적 구조를 돌려준다`() = runTest {
    val result = crawlers { """{"listSmrCretSumTabFac":[],"listSmrCretSumTabYearSmr":[],"selectSmrCretSumTabSjTotal":null}""" }
      .scrapeCredits("24020044")

    assertEquals(emptyList(), result.creditDTOs)
    assertEquals(
      buildJsonObject {
        put("listSmrCretSumTabYearSmr", JsonArray(emptyList()))
        put(
          "selectSmrCretSumTabSjTotal",
          buildJsonObject { put("gainPoint", "0"); put("applPoint", "0"); put("gainAvmk", "0"); put("gainTavgPont", "0") },
        )
      },
      result.gradeResponse,
    )
  }

  @Test
  fun `학기 성적 상세 조회가 실패하면 그 학기만 건너뛰고 학기 정보를 붙인다`() = runTest {
    val result = crawlers { request ->
      if (request.url.encodedPath == "/smrCretSum/listSmrCretSumTabYearSmrStud.do") {
        """{"listSmrCretSumTabYearSmr":[{"cretGainYear":"2025","cretSmrCd":"10"},{"cretGainYear":"2025","cretSmrCd":"20"}],
          |"selectSmrCretSumTabSjTotal":{"gainPoint":null,"applPoint":"3"}}""".trimMargin()
      } else if (request.jsonBody()["cretSmrCd"] == JsonPrimitive("10")) {
        """{"listSmrCretSumTabSubjt":[{"subjtCd":"04048","gainPoint":3}]}"""
      } else {
        "{깨진 응답"
      }
    }.scrapeCredits("24020044")

    assertEquals(
      listOf(buildJsonObject { put("subjtCd", "04048"); put("gainPoint", 3); put("cretGainYear", "2025"); put("cretSmrCd", "10") }),
      result.creditDTOs,
    )
    assertEquals(1, result.warnings.size)
    assertTrue(result.warnings.single().startsWith("2025-20"))
    // 합계의 gainPoint가 null이면 "0"으로 채우고 나머지 응답 필드는 그대로 둔다.
    assertEquals(
      buildJsonObject { put("gainPoint", "0"); put("applPoint", "3") },
      result.gradeResponse["selectSmrCretSumTabSjTotal"]!!.jsonObject,
    )
  }
}
