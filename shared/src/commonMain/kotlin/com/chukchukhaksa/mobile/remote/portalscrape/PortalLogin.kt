package com.chukchukhaksa.mobile.remote.portalscrape

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeErrorCode
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeException
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.parameters

internal object SuwonPortalUrls {
  /** 포털 첫 화면(index.html)이 mainFrame으로 띄우는 로그인 화면. */
  const val PORTAL_FRAME = "https://portal.suwon.ac.kr/enview/index.jsp"
  const val PORTAL_ORIGIN = "https://portal.suwon.ac.kr"

  /** 로그인 화면의 enviewLogin()이 폼을 전송하는 주소. */
  const val LOGIN =
    "https://portal.suwon.ac.kr/enpass/login?_epLogin_=enview&service=https://portal.suwon.ac.kr/enview/user/enpassLoginProcess.face"

  const val ACADEMIC_SSO = "https://info.suwon.ac.kr/sso_security_check"
  const val ACADEMIC_BASE = "https://info.suwon.ac.kr"
  const val ACADEMIC_REFERER =
    "https://info.suwon.ac.kr/websquare/websquare_mobile.html?w2xPath=/views/usw/sa/hj/SA_HJ_1230.xml&menuSeq=3818&progSeq=1117"
}

private const val TICKET_PARAMETER = "epTicket"
private const val ERROR_CODE_PARAMETER = "epErrCode"

/** enpass가 로그인되지 않았을 때 티켓 자리에 넣는 값. */
private const val NOT_LOGGED_IN_TICKET = "LOG"

// 스크래퍼(portalLogin.ts)가 alert 문구로 판별하던 실패 메시지.
private const val ACCOUNT_LOCKED_MESSAGE = "연속 5회 잘못 입력하셨습니다"
private val AUTH_FAILED_MESSAGES = listOf("아이디 또는 비밀번호를 잘못 입력하셨습니다", "유효하지 않은 사용자입니다")

// 로그인 실패 후 돌아온 포털 화면은 `var errorMessage = "...";` 를 alert로 띄운다.
private val PORTAL_ERROR_MESSAGE = Regex("""var\s+errorMessage\s*=\s*"([^"]*)"""")

/**
 * 포털에 로그인한다. 로그인 화면의 enviewLogin()이 만드는 폼을 그대로 전송하고,
 * enpass가 리다이렉트 주소에 담아 주는 epTicket으로 성공 여부를 판별한다.
 * 성공하면 enpass 세션 쿠키(ENPASSTGC)가 저장되어 이후 학사 SSO에 쓰인다.
 */
internal suspend fun HttpClient.loginToPortal(studentId: String, password: String) {
  // 브라우저처럼 로그인 화면을 먼저 열어 포털 세션 쿠키를 받는다.
  followRedirects(get(SuwonPortalUrls.PORTAL_FRAME))

  // enviewLogin()은 학번을 대문자로 바꾸고 학번·비밀번호의 앞뒤 공백을 제거한 뒤 전송한다.
  val userId = studentId.trim().uppercase()
  val userPassword = password.trim()
  val response = submitForm(
    url = SuwonPortalUrls.LOGIN,
    formParameters = parameters {
      append("_enpass_login_", "submit")
      append("gateway", "true")
      append("epsn", "")
      append("password", userPassword)
      append("username", userId)
      append("langKnd", "ko")
      append("loginCheck", "Y")
      append("userId", userId)
      append("pwd", userPassword)
      append("userId2", "")
    },
  ) {
    header(HttpHeaders.Referrer, SuwonPortalUrls.PORTAL_FRAME)
    header(HttpHeaders.Origin, SuwonPortalUrls.PORTAL_ORIGIN)
  }

  val chain = followRedirects(response)
  val ticketUrl = chain.visitedUrls.firstOrNull { it.parameters.contains(TICKET_PARAMETER) }
    ?: throw PortalScrapeException(
      errorCode = PortalScrapeErrorCode.BUSINESS_RULE_VIOLATION,
      message = "포털 로그인 응답에서 epTicket을 찾을 수 없습니다.",
      retryable = false,
    )
  if (ticketUrl.parameters[TICKET_PARAMETER] != NOT_LOGGED_IN_TICKET) return

  val portalMessage = PORTAL_ERROR_MESSAGE.find(chain.response.bodyAsText())
    ?.groupValues?.get(1)
    ?.takeIf { it.isNotBlank() && it != "null" }
  throw portalLoginFailure(portalMessage, ticketUrl.parameters[ERROR_CODE_PARAMETER])
}

internal fun portalLoginFailure(portalMessage: String?, portalErrorCode: String?): PortalScrapeException = when {
  portalMessage != null && ACCOUNT_LOCKED_MESSAGE in portalMessage -> PortalScrapeException(
    errorCode = PortalScrapeErrorCode.PORTAL_ACCOUNT_LOCKED,
    message = "계정이 잠겼습니다. 포털사이트로 돌아가서 학번/사번 찾기 및 비밀번호 재발급을 진행해주세요.",
    retryable = false,
  )

  portalMessage != null && AUTH_FAILED_MESSAGES.any { it in portalMessage } -> PortalScrapeException(
    errorCode = PortalScrapeErrorCode.PORTAL_AUTH_FAILED,
    message = "아이디나 비밀번호가 일치하지 않습니다.\n학교 홈페이지에서 확인해주세요.",
    retryable = false,
  )

  else -> PortalScrapeException(
    errorCode = PortalScrapeErrorCode.PORTAL_AUTH_FAILED,
    message = "포털 로그인에 실패했습니다. (" +
      listOfNotNull(portalMessage, portalErrorCode?.let { "epErrCode=$it" }).joinToString().ifEmpty { "사유 미확인" } +
      ")",
    retryable = false,
  )
}

/**
 * 포털 로그인 세션을 학사 시스템(info.suwon.ac.kr)으로 넘긴다.
 * sso_security_check → enpass → sso_security_check?epTicket=... 리다이렉트를 거치며 학사 세션 쿠키가 저장된다.
 */
internal suspend fun HttpClient.handOffToAcademicSystem() {
  val chain = followRedirects(get(SuwonPortalUrls.ACADEMIC_SSO))
  val ticket = chain.visitedUrls.firstNotNullOfOrNull { it.parameters[TICKET_PARAMETER] }
  if (ticket == null || ticket == NOT_LOGGED_IN_TICKET) {
    throw PortalScrapeException(
      errorCode = PortalScrapeErrorCode.BUSINESS_RULE_VIOLATION,
      message = "학사 시스템 SSO 연결에 실패했습니다. (epTicket=${ticket ?: "없음"})",
      retryable = false,
    )
  }
}
