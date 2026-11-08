package com.chukchukhaksa.mobile.remote.portalscrape

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject

/**
 * 학사 API 요청 한 번과 그 결과. 응답을 받았으면 [httpStatus]와 [bodyText]에 받은 그대로 담고,
 * 응답을 받지 못했으면 [transportError]에 원인을 담는다.
 */
internal class PortalAcademicExchange(
  val path: String,
  val request: JsonObject,
  val httpStatus: Int?,
  val bodyText: String?,
  val transportError: Throwable?,
)

/**
 * 한 번의 스크래핑에서 주고받은 학사 API 원문. 참고 석차 원본(`referenceRankingRaw`)은 응답 텍스트를 그대로 보내야 해서,
 * 학사 자료를 조회하며 받은 응답을 여기에 남겨 두고 다시 조회하지 않고 재사용한다.
 * 수강·성적 조회가 동시에 기록하므로 잠금으로 보호한다.
 */
internal class PortalAcademicExchangeLog {
  private val mutex = Mutex()
  private val exchanges = mutableListOf<PortalAcademicExchange>()

  suspend fun record(exchange: PortalAcademicExchange) = mutex.withLock { exchanges += exchange }

  suspend fun snapshot(): List<PortalAcademicExchange> = mutex.withLock { exchanges.toList() }
}

private val ACADEMIC_JSON = ContentType.parse("application/json;charset=UTF-8")

/** 로그인된 학사 세션으로 학사 API를 호출한다. 응답을 해석하지 않고 원문을 돌려주며, [log]가 있으면 기록한다. */
internal class PortalAcademicApi(
  private val client: HttpClient,
  private val log: PortalAcademicExchangeLog? = null,
) {
  suspend fun exchange(path: String, body: JsonObject): PortalAcademicExchange {
    val exchange = try {
      val response = client.post(SuwonPortalUrls.ACADEMIC_BASE + path) {
        header(HttpHeaders.Accept, "application/json")
        header(HttpHeaders.Referrer, SuwonPortalUrls.ACADEMIC_REFERER)
        setBody(TextContent(body.toString(), ACADEMIC_JSON))
      }
      Napier.d(tag = LOG_TAG) { "POST $path → ${response.status.value}" }
      PortalAcademicExchange(path, body, response.status.value, response.bodyAsText(), transportError = null)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      PortalAcademicExchange(path, body, httpStatus = null, bodyText = null, transportError = e)
    }
    log?.record(exchange)
    return exchange
  }
}
