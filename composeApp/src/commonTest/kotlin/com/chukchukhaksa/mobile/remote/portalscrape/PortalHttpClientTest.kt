package com.chukchukhaksa.mobile.remote.portalscrape

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.request.get
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortalHttpClientTest {

  @Test
  fun `http 리다이렉트는 https로 승격한다`() {
    val resolved = resolveRedirectUrl(
      base = Url("https://info.suwon.ac.kr/sso_security_check"),
      location = "http://portal.suwon.ac.kr/enpass/login?gateway=client&service=https://info.suwon.ac.kr/sso_security_check",
    )

    assertEquals(
      "https://portal.suwon.ac.kr/enpass/login?gateway=client&service=https://info.suwon.ac.kr/sso_security_check",
      resolved.toString(),
    )
  }

  @Test
  fun `상대 경로 Location은 현재 URL 기준으로 풀고 이전 쿼리를 남기지 않는다`() {
    val base = Url("https://portal.suwon.ac.kr/enview/user/enpassLoginProcess.face?epTicket=T1")

    assertEquals(
      "https://portal.suwon.ac.kr/enview/portal/USW_STU/USW_STU_DEFAULT.page",
      resolveRedirectUrl(base, "/enview/portal/USW_STU/USW_STU_DEFAULT.page").toString(),
    )
    assertEquals(
      "https://portal.suwon.ac.kr/enview/user/next.face?a=1",
      resolveRedirectUrl(base, "next.face?a=1").toString(),
    )
    assertEquals("https://info.suwon.ac.kr/main", resolveRedirectUrl(base, "//info.suwon.ac.kr/main").toString())
  }

  @Test
  fun `세미콜론 경로 파라미터가 있는 Location을 그대로 유지한다`() {
    val resolved = resolveRedirectUrl(
      base = Url("https://portal.suwon.ac.kr/enview/index.jsp"),
      location = "https://portal.suwon.ac.kr/enview/portal/;jsessionid=ABC.uswptp01",
    )

    assertEquals("https://portal.suwon.ac.kr/enview/portal/;jsessionid=ABC.uswptp01", resolved.toString())
  }

  @Test
  fun `Path가 없는 쿠키는 요청 경로의 디렉터리를 기본 Path로 쓴다`() = runTest {
    val storage = RfcDefaultPathCookiesStorage(AcceptAllCookiesStorage())

    storage.addCookie(Url("https://info.suwon.ac.kr/sso_security_check?epTicket=T"), Cookie("userNm", "TESTER"))
    storage.addCookie(Url("https://portal.suwon.ac.kr/enpass/login"), Cookie("ENPASSTGC", "TGC"))

    assertTrue(storage.get(Url("https://info.suwon.ac.kr/scrgBas/selectScrgBas.do")).any { it.name == "userNm" })
    assertTrue(storage.get(Url("https://portal.suwon.ac.kr/enpass/login?gateway=client")).any { it.name == "ENPASSTGC" })
    assertTrue(storage.get(Url("https://portal.suwon.ac.kr/enview/index.jsp")).none { it.name == "ENPASSTGC" })
  }

  @Test
  fun `기본 쿠키 Path 계산`() {
    assertEquals("/", defaultCookiePath("/sso_security_check"))
    assertEquals("/enpass", defaultCookiePath("/enpass/login"))
    assertEquals("/", defaultCookiePath(""))
  }

  @Test
  fun `포털이 24시로 찍은 Expires를 내려줘도 요청이 실패하지 않고 쿠키를 다음 요청에 보낸다`() = runTest {
    val sentCookies = mutableListOf<String?>()
    val engine = MockEngine { request ->
      sentCookies += request.headers[HttpHeaders.Cookie]
      respond(
        content = "",
        status = HttpStatusCode.OK,
        headers = headersOf(
          HttpHeaders.SetCookie,
          listOf("TRACK=T1; Expires=Tue, 12-Oct-2027 24:13:2 GMT; Path=/", "JSESSIONID=S1; Path=/"),
        ),
      )
    }
    val client = HttpClient(engine) { configurePortalClient() }

    client.get("https://portal.suwon.ac.kr/enview/index.jsp")
    client.get("https://portal.suwon.ac.kr/enview/index.jsp")

    assertEquals(listOf(null, "TRACK=T1; JSESSIONID=S1"), sentCookies)
  }

  @Test
  fun `해석할 수 없는 Expires만 무시하고 나머지 속성은 살린다`() {
    val cookie = assertNotNull(parseSetCookie("TRACK=T1; Expires=Tue, 12-Oct-2027 24:13:2 GMT; Path=/enview; Secure"))

    assertEquals("TRACK", cookie.name)
    assertEquals("T1", cookie.value)
    assertEquals("/enview", cookie.path)
    assertTrue(cookie.secure)
    assertNull(cookie.expires)
  }

  @Test
  fun `한 헤더에 쉼표로 이어 붙은 Set-Cookie를 쿠키별로 나눈다`() {
    // iOS(NSHTTPURLResponse)는 여러 Set-Cookie를 쉼표로 이어 붙여 하나의 헤더로 넘긴다.
    val cookies = parseSetCookieHeaders(
      listOf(
        "A=1; Expires=Tue, 12-Oct-2027 24:13:2 GMT; Path=/, B=2; Expires=Wed, 13-Oct-2027 01:00:00 GMT, C=x,y; Path=/c",
      ),
    )

    assertEquals(listOf("A" to "1", "B" to "2", "C" to "x,y"), cookies.map { it.name to it.value })
    assertNull(cookies[0].expires)
    assertNotNull(cookies[1].expires)
    assertEquals("/c", cookies[2].path)
  }

  @Test
  fun `이름이 없는 Set-Cookie는 건너뛴다`() {
    assertNull(parseSetCookie(""))
  }
}
