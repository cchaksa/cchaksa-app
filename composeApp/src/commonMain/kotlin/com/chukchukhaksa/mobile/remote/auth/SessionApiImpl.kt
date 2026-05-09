package com.chukchukhaksa.mobile.remote.auth

import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.common.designsystem.component.webview.toLogString
import com.chukchukhaksa.mobile.remote.auth.dto.SessionExchangeErrorDto
import com.chukchukhaksa.mobile.remote.auth.dto.SessionExchangeRequest
import com.chukchukhaksa.mobile.remote.auth.dto.SessionExchangeSuccessDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.parseServerSetCookieHeader
import kotlin.coroutines.cancellation.CancellationException

class SessionApiImpl(
  private val client: HttpClient,
  private val baseUrl: String,
) : SessionApi {

  private val sessionHost: String =
    baseUrl.substringAfter("://").substringBefore("/").substringBefore(":")

  override suspend fun exchange(
    accessToken: String,
    refreshToken: String,
  ): Result<SessionExchangeResult> = try {
    Napier.d(tag = "SessionApi") {
      "POST $baseUrl$SESSION_PATH (accessToken.len=${accessToken.length}, refreshToken.len=${refreshToken.length})"
    }
    val response: HttpResponse = client.post("$baseUrl$SESSION_PATH") {
      contentType(ContentType.Application.Json)
      setBody(SessionExchangeRequest(accessToken = accessToken, refreshToken = refreshToken))
    }
    Napier.d(tag = "SessionApi") { "← status=${response.status.value}" }
    when (response.status) {
      HttpStatusCode.OK -> handleSuccess(response)
      HttpStatusCode.BadRequest -> handleBadRequest(response)
      else -> Result.failure(
        SessionExchangeError.Unknown(httpStatus = response.status.value, throwable = null),
      )
    }
  } catch (e: CancellationException) {
    throw e
  } catch (t: Throwable) {
    Napier.e(tag = "SessionApi", throwable = t) { "Exchange request failed" }
    Result.failure(SessionExchangeError.Unknown(httpStatus = null, throwable = t))
  }

  private suspend fun handleSuccess(response: HttpResponse): Result<SessionExchangeResult> {
    val success: SessionExchangeSuccessDto = try {
      response.body()
    } catch (e: CancellationException) {
      throw e
    } catch (t: Throwable) {
      Napier.w(tag = "SessionApi", throwable = t) { "Success body decode failed" }
      return Result.failure(SessionExchangeError.Unknown(httpStatus = 200, throwable = t))
    }

    val rawSetCookieHeaders = response.headers.getAll(HttpHeaders.SetCookie).orEmpty()
    Napier.d(tag = "SessionApi") {
      "Set-Cookie headers received (count=${rawSetCookieHeaders.size}): ${rawSetCookieHeaders.joinToString(" || ")}"
    }
    val cookies = parseSessionCookies(rawSetCookieHeaders)
    Napier.d(tag = "SessionApi") {
      "Parsed cookies (count=${cookies.size}): ${cookies.joinToString(" || ") { it.toLogString() }}"
    }
    return Result.success(
      SessionExchangeResult(
        cookies = cookies,
        isPortalLinked = success.isPortalLinked,
      ),
    )
  }

  private suspend fun handleBadRequest(response: HttpResponse): Result<SessionExchangeResult> {
    val error: SessionExchangeErrorDto = try {
      response.body()
    } catch (e: CancellationException) {
      throw e
    } catch (t: Throwable) {
      Napier.w(tag = "SessionApi", throwable = t) { "400 body decode failed" }
      return Result.failure(SessionExchangeError.Unknown(httpStatus = 400, throwable = t))
    }
    val mapped = when (error.error) {
      "MISSING_ACCESS_TOKEN" -> SessionExchangeError.MissingAccessToken
      "MISSING_REFRESH_TOKEN" -> SessionExchangeError.MissingRefreshToken
      "INVALID_JSON" -> SessionExchangeError.InvalidJson
      else -> SessionExchangeError.Unknown(httpStatus = 400, throwable = null)
    }
    return Result.failure(mapped)
  }

  private fun parseSessionCookies(headers: List<String>): List<WebViewCookie> {
    if (headers.isEmpty()) return emptyList()
    return headers.mapNotNull { header ->
      try {
        val cookie = parseServerSetCookieHeader(header)
        if (cookie.name != SESSION_COOKIE_NAME) return@mapNotNull null
        val resolvedDomain = cookie.domain?.takeIf { it.isNotBlank() } ?: sessionHost
        WebViewCookie(
          name = cookie.name,
          value = cookie.value,
          domain = resolvedDomain,
          path = cookie.path ?: "/",
          secure = cookie.secure,
          httpOnly = cookie.httpOnly,
          sameSite = cookie.extensions.entries
            .firstOrNull { it.key.equals("SameSite", ignoreCase = true) }
            ?.value
            ?: "Lax",
          expiresEpochSeconds = cookie.expires?.timestamp?.let { it / 1000L },
        )
      } catch (e: CancellationException) {
        throw e
      } catch (t: Throwable) {
        Napier.w(tag = "SessionApi", throwable = t) { "Set-Cookie parse failed: $header" }
        null
      }
    }
  }

  private companion object {
    const val SESSION_PATH = "/api/session"
    const val SESSION_COOKIE_NAME = "cchaksa_session"
  }
}
