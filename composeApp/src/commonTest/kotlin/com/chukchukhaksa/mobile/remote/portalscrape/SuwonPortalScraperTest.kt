package com.chukchukhaksa.mobile.remote.portalscrape

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeErrorCode
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeException
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeStep
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
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
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class SuwonPortalScraperTest {

  private val portal = FakeSuwonPortal()

  // Ktor 요청은 실제 IO 스레드에서 끝나므로, runTest의 가상 시간 대신 실제 시간으로 전체 제한 시간을 잰다.
  private suspend fun scrape(
    studentId: String,
    password: String,
    scraper: SuwonPortalScraper = SuwonPortalScraper(clientFactory = portal::client),
    onProgress: (PortalScrapeProgress) -> Unit = {},
  ) = withContext(Dispatchers.Default) { scraper.scrape(studentId, password, onProgress) }

  @Test
  fun `포털 로그인부터 학기별 병합까지 스크래퍼와 같은 결과를 만든다`() = runTest {
    val progress = mutableListOf<PortalScrapeProgress>()

    val result = scrape(" 24020044 ", "secret ", onProgress = { progress += it })

    // 학생 정보: 스크래퍼가 고르는 필드만 담는다.
    assertEquals(JsonPrimitive("테스트 학생"), result.student["studNm"])
    assertTrue("studIidno" !in result.student)
    // 수강 point 3은 유지하고, null이던 채플 point는 성적 gainPoint 1로 보정한다.
    val courses = result.semesters.single().jsonObject["courses"]!!.jsonArray.map { it.jsonObject }
    assertEquals(listOf(JsonPrimitive(3), JsonPrimitive(1)), courses.map { it["point"] })
    assertEquals(JsonPrimitive("A+"), courses[0]["cretGrdCd"])
    // 성적 요약 응답은 원본 그대로 academicRecords에 담는다.
    assertEquals(FakeSuwonPortal.defaultGradeSummary(), result.academicRecords)
    assertEquals(JsonArray(emptyList()), result.designatedCourses)
    assertEquals(listOf("student", "semesters", "academicRecords", "designatedCourses"), result.toJsonObject().keys.toList())

    // http로 내려온 SSO 리다이렉트는 https로 승격해서 요청했다.
    assertTrue(portal.requests.all { it.url.protocol == URLProtocol.HTTPS })
    // 비편입생이므로 지정과목 API는 호출하지 않는다.
    assertTrue(portal.requestsTo("/precpSbjt/listPrecpSbjt.do").isEmpty())
    assertTrue(PortalScrapeProgress.Skipped(PortalScrapeStep.DESIGNATED_COURSES, "편입생이 아님") in progress)
    assertEquals(
      PortalScrapeStep.entries.toSet() - PortalScrapeStep.DESIGNATED_COURSES,
      progress.filterIsInstance<PortalScrapeProgress.Finished>().map { it.step }.toSet(),
    )
  }

  @Test
  fun `로그인 폼은 포털의 enviewLogin과 같은 값으로 전송한다`() = runTest {
    scrape("ab12 ", " pw!1 ")

    val login = portal.requestsTo("/enpass/login").first { it.method == HttpMethod.Post }
    val form = (login.body as FormDataContent).formData
    assertEquals("enview", login.url.parameters["_epLogin_"])
    assertEquals("AB12", form["userId"])
    assertEquals("AB12", form["username"])
    assertEquals("pw!1", form["pwd"])
    assertEquals("pw!1", form["password"])
    assertEquals("Y", form["loginCheck"])
    assertEquals("submit", form["_enpass_login_"])
    // 학사 API에는 공백만 제거한 학번을 보낸다.
    assertEquals(JsonPrimitive("ab12"), portal.requestsTo("/scrgBas/selectScrgBas.do").single().jsonBody()["sno"])
  }

  @Test
  fun `존재하지 않는 사용자면 비밀번호 불일치 오류를 낸다`() = runTest {
    portal.loginOutcome = FakeSuwonPortal.LoginOutcome.INVALID_USER

    val error = assertFailsWith<PortalScrapeException> { scrape("00000000", "wrong") }

    assertEquals(PortalScrapeErrorCode.PORTAL_AUTH_FAILED, error.errorCode)
    assertEquals("아이디나 비밀번호가 일치하지 않습니다.\n학교 홈페이지에서 확인해주세요.", error.message)
    assertTrue(portal.requestsTo("/sso_security_check").isEmpty())
  }

  @Test
  fun `5회 실패로 잠긴 계정은 계정 잠김 오류를 낸다`() = runTest {
    portal.loginOutcome = FakeSuwonPortal.LoginOutcome.ACCOUNT_LOCKED

    val error = assertFailsWith<PortalScrapeException> { scrape("24020044", "wrong") }

    assertEquals(PortalScrapeErrorCode.PORTAL_ACCOUNT_LOCKED, error.errorCode)
  }

  @Test
  fun `알 수 없는 로그인 실패 문구는 포털 메시지와 오류 코드를 그대로 전한다`() {
    val error = portalLoginFailure(portalMessage = "비밀번호 변경 기간입니다.", portalErrorCode = "A09")

    assertEquals(PortalScrapeErrorCode.PORTAL_AUTH_FAILED, error.errorCode)
    assertEquals("포털 로그인에 실패했습니다. (비밀번호 변경 기간입니다., epErrCode=A09)", error.message)
  }

  @Test
  fun `학사 SSO가 티켓을 주지 않으면 SSO 실패로 끝낸다`() = runTest {
    portal.ssoIssuesTicket = false

    val error = assertFailsWith<PortalScrapeException> { scrape("24020044", "secret") }

    assertEquals(PortalScrapeErrorCode.BUSINESS_RULE_VIOLATION, error.errorCode)
    assertEquals("학사 시스템 SSO 연결에 실패했습니다. (epTicket=LOG)", error.message)
  }

  @Test
  fun `학번이나 비밀번호가 비어 있으면 요청하지 않는다`() = runTest {
    val error = assertFailsWith<PortalScrapeException> { scrape(" ", "secret") }

    assertEquals(PortalScrapeErrorCode.INVALID_PAYLOAD, error.errorCode)
    assertTrue(portal.requests.isEmpty())
  }

  @Test
  fun `전체 제한 시간을 넘기면 재시도 가능한 시간 초과 오류다`() = runTest {
    val slowScraper = SuwonPortalScraper(
      clientFactory = { HttpClient(MockEngine { awaitCancellation() }) { configurePortalClient() } },
      totalTimeout = 300.milliseconds,
    )

    val error = assertFailsWith<PortalScrapeException> { scrape("24020044", "secret", scraper = slowScraper) }

    assertEquals(PortalScrapeErrorCode.PORTAL_TIMEOUT, error.errorCode)
    assertTrue(error.retryable)
  }

  @Test
  fun `네트워크 오류는 재시도 가능한 일시 오류로 분류한다`() = runTest {
    val offlineScraper = SuwonPortalScraper(
      clientFactory = { HttpClient(MockEngine { throw IOException("Unable to resolve host") }) { configurePortalClient() } },
    )

    val error = assertFailsWith<PortalScrapeException> { scrape("24020044", "secret", scraper = offlineScraper) }

    assertEquals(PortalScrapeErrorCode.PORTAL_TEMPORARY_UNAVAILABLE, error.errorCode)
    assertTrue(error.retryable)
  }

  // scrapeJob.spec.ts
  private val designatedCourse = buildJsonObject { put("subjtCd", "SUBJ001"); put("point", 3) }
  private val gradeResponse = buildJsonObject { put("listSmrCretSumTabYearSmr", JsonArray(emptyList())) }

  private inner class FakeCrawlers(
    private val enscDvcd: String,
    private val pendingStudent: CompletableDeferred<Unit>? = null,
  ) : PortalCrawlers {
    val calls = mutableListOf<String>()
    var designatedError: Exception? = null

    override suspend fun scrapeStudent(studentId: String): JsonObject {
      calls += "student"
      pendingStudent?.await()
      return buildJsonObject { put("sno", studentId); put("enscDvcd", enscDvcd); put("flangPassGb", "미통과") }
    }

    override suspend fun scrapeCourses(studentId: String): List<JsonObject> {
      calls += "courses"
      return emptyList()
    }

    override suspend fun scrapeCredits(studentId: String): PortalCreditResult {
      calls += "credits"
      return PortalCreditResult(emptyList(), gradeResponse)
    }

    override suspend fun scrapeDesignatedCourses(studentId: String): JsonArray {
      calls += "designatedCourses"
      designatedError?.let { throw it }
      return buildJsonArray { add(designatedCourse) }
    }
  }

  @Test
  fun `편입생이면 지정과목을 한 번 조회해 결과에 포함한다`() = runTest {
    val crawlers = FakeCrawlers(enscDvcd = "2")

    val result = scrapeAuthenticatedData(crawlers, "24020044")

    assertEquals(1, crawlers.calls.count { it == "designatedCourses" })
    assertEquals(buildJsonArray { add(designatedCourse) }, result.designatedCourses)
    assertEquals(JsonPrimitive("2"), result.student["enscDvcd"])
    assertEquals(JsonPrimitive("미통과"), result.student["flangPassGb"])
  }

  @Test
  fun `비편입생이면 지정과목 API를 호출하지 않고 빈 배열을 반환한다`() = runTest {
    val crawlers = FakeCrawlers(enscDvcd = "1")

    val result = scrapeAuthenticatedData(crawlers, "24020044")

    assertTrue("designatedCourses" !in crawlers.calls)
    assertEquals(JsonArray(emptyList()), result.designatedCourses)
  }

  @Test
  fun `지정과목 형식 오류는 다른 수집 결과로 대체하지 않고 전체 작업 실패로 전파한다`() = runTest {
    val schemaError = PortalScrapeException(PortalScrapeErrorCode.PORTAL_RESPONSE_SCHEMA_MISMATCH, "지정과목 응답 형식 오류", false)
    val crawlers = FakeCrawlers(enscDvcd = "2").apply { designatedError = schemaError }

    val error = assertFailsWith<PortalScrapeException> { scrapeAuthenticatedData(crawlers, "24020044") }

    assertSame(schemaError, error)
  }

  @Test
  fun `수강과 성적 요청은 학생 정보 완료를 기다리지 않고 시작한다`() = runTest {
    val pendingStudent = CompletableDeferred<Unit>()
    val crawlers = FakeCrawlers(enscDvcd = "2", pendingStudent = pendingStudent)

    val result = async { scrapeAuthenticatedData(crawlers, "24020044") }
    testScheduler.runCurrent()
    assertEquals(listOf("student", "courses", "credits"), crawlers.calls)

    pendingStudent.complete(Unit)
    result.await()
    assertEquals(listOf("student", "courses", "credits", "designatedCourses"), crawlers.calls)
  }

  @Test
  fun `학기 키와 편입생 판별은 스크래퍼의 문자열 비교를 따른다`() {
    assertTrue(buildJsonObject { put("enscDvcd", "2") }.isTransferStudent())
    // 숫자 2는 JS의 `=== "2"`와 같이 편입생으로 보지 않는다.
    assertTrue(!buildJsonObject { put("enscDvcd", 2) }.isTransferStudent())
    assertEquals("null-undefined", "${JsonNull.toJsString()}-${null.toJsString()}")
    assertEquals("2025", JsonPrimitive(2025.0).toJsString())
  }
}
