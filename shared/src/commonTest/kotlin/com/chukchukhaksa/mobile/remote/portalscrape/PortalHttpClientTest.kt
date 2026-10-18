package com.chukchukhaksa.mobile.remote.portalscrape

import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
