package com.chukchukhaksa.mobile.remote.portalscrape

import com.chukchukhaksa.mobile.common.kmp.httpClientEngineFactory
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeErrorCode
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeException
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Cookie
import io.ktor.http.DEFAULT_PORT
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.takeFrom

// 스크래퍼의 학사 API 요청 헤더와 같은 값. 포털·SSO 요청에도 같은 값을 쓴다.
internal const val PORTAL_USER_AGENT = "Mozilla/5.0"

private const val MAX_REDIRECTS = 10
private val REDIRECT_STATUS_CODES = setOf(
  HttpStatusCode.MovedPermanently,
  HttpStatusCode.Found,
  HttpStatusCode.SeeOther,
  HttpStatusCode.TemporaryRedirect,
  HttpStatusCode.PermanentRedirect,
)
private val URL_SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:")

/** 스크래핑 1회의 쿠키가 다음 실행으로 이어지지 않도록 실행할 때마다 새 클라이언트를 만든다. */
internal fun createPortalHttpClient(): HttpClient = HttpClient(httpClientEngineFactory) {
  configurePortalClient()
}

internal fun HttpClientConfig<*>.configurePortalClient() {
  // 학사 SSO가 http://portal.suwon.ac.kr 로 리다이렉트한다. Android cleartext 차단과 iOS ATS를 피하려고
  // 리다이렉트를 직접 따라가며 https로 승격한다([followRedirects]).
  followRedirects = false
  expectSuccess = false
  install(HttpTimeout) {
    requestTimeoutMillis = 30_000
    connectTimeoutMillis = 15_000
    socketTimeoutMillis = 30_000
  }
  install(HttpCookies) {
    storage = RfcDefaultPathCookiesStorage(AcceptAllCookiesStorage())
  }
  install(UserAgent) {
    agent = PORTAL_USER_AGENT
  }
}

/**
 * Ktor 기본 저장소는 Path 속성이 없는 쿠키에 요청 경로 전체를 Path로 넣어서, 학사 API 경로로 쿠키가 전송되지 않을 수 있다.
 * 브라우저(RFC 6265 5.1.4)처럼 요청 경로의 디렉터리를 기본 Path로 채운 뒤 저장한다.
 */
internal class RfcDefaultPathCookiesStorage(
  private val delegate: CookiesStorage,
) : CookiesStorage by delegate {
  override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
    val normalized = if (cookie.path?.startsWith("/") == true) {
      cookie
    } else {
      cookie.copy(path = defaultCookiePath(requestUrl.encodedPath))
    }
    delegate.addCookie(requestUrl, normalized)
  }
}

internal fun defaultCookiePath(requestPath: String): String {
  val lastSlash = requestPath.lastIndexOf('/')
  return if (!requestPath.startsWith("/") || lastSlash <= 0) "/" else requestPath.substring(0, lastSlash)
}

internal class RedirectChain(
  val response: HttpResponse,
  /** 따라간 리다이렉트 대상 URL. epTicket 판별에 쓴다. */
  val visitedUrls: List<Url>,
)

/** 브라우저처럼 리다이렉트를 GET으로 따라간다. 각 응답의 Set-Cookie는 [HttpCookies]가 저장한다. */
internal suspend fun HttpClient.followRedirects(initial: HttpResponse): RedirectChain {
  var response = initial
  val visitedUrls = mutableListOf<Url>()
  repeat(MAX_REDIRECTS) {
    val location = response.headers[HttpHeaders.Location]
    if (response.status !in REDIRECT_STATUS_CODES || location == null) {
      return RedirectChain(response, visitedUrls)
    }
    val next = resolveRedirectUrl(response.call.request.url, location)
    Napier.d(tag = LOG_TAG) { "redirect ${response.status.value} → ${next.maskedForLog()}" }
    visitedUrls += next
    response = get(next)
  }
  throw PortalScrapeException(
    errorCode = PortalScrapeErrorCode.BUSINESS_RULE_VIOLATION,
    message = "포털 리다이렉트가 ${MAX_REDIRECTS}회를 넘었습니다.",
    retryable = false,
  )
}

/** Location을 현재 URL 기준으로 풀고, http는 https로 승격한다. */
internal fun resolveRedirectUrl(base: Url, location: String): Url {
  val builder = when {
    location.startsWith("//") -> URLBuilder("${base.protocol.name}:$location")
    URL_SCHEME.containsMatchIn(location) -> URLBuilder(location)
    else -> URLBuilder().apply {
      protocol = base.protocol
      host = base.host
      port = base.specifiedPort
      val absolutePath = if (location.startsWith("/")) {
        location
      } else {
        "${base.encodedPath.substringBeforeLast('/')}/$location"
      }
      takeFrom(absolutePath)
    }
  }
  if (builder.protocol == URLProtocol.HTTP) {
    builder.protocol = URLProtocol.HTTPS
    if (builder.port == URLProtocol.HTTP.defaultPort) builder.port = DEFAULT_PORT
  }
  return builder.build()
}

/** 로그에 티켓·세션 값이 남지 않도록 가린다. */
internal fun Url.maskedForLog(): String =
  toString().replace(Regex("(epTicket|ticket|jsessionid)=[^&;]*", RegexOption.IGNORE_CASE), "$1=***")

internal const val LOG_TAG = "PortalScrape"
