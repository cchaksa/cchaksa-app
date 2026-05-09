package com.chukchukhaksa.mobile.remote.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SessionApiImplTest {

  private val baseUrl = "https://dv.cchaksa.com"

  private fun buildClient(engine: MockEngine): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
      json(
        Json {
          ignoreUnknownKeys = true
          isLenient = true
        },
      )
    }
    defaultRequest {
      contentType(ContentType.Application.Json)
    }
  }

  @Test
  fun `200 with set-cookie returns cookie list`() = runTest {
    val engine = MockEngine { _ ->
      respond(
        content = ByteReadChannel("""{"ok":true,"isPortalLinked":false}"""),
        status = HttpStatusCode.OK,
        headers = headersOf(
          HttpHeaders.ContentType to listOf("application/json"),
          HttpHeaders.SetCookie to listOf(
            "cchaksa_session=abc; Domain=.cchaksa.com; Path=/; Secure; HttpOnly; SameSite=Lax",
          ),
        ),
      )
    }
    val api = SessionApiImpl(client = buildClient(engine), baseUrl = baseUrl)

    val result = api.exchange(accessToken = "ac", refreshToken = "re")

    assertTrue(result.isSuccess)
    val payload = result.getOrThrow()
    assertEquals(false, payload.isPortalLinked)
    assertEquals(1, payload.cookies.size)
    val cookie = payload.cookies.first()
    assertEquals("cchaksa_session", cookie.name)
    assertEquals("abc", cookie.value)
    assertEquals(true, cookie.secure)
    assertEquals(true, cookie.httpOnly)
    assertEquals(".cchaksa.com", cookie.domain)
    assertEquals("Lax", cookie.sameSite)
  }

  @Test
  fun `400 missing access token maps to MissingAccessToken`() = runTest {
    val engine = MockEngine { _ ->
      respond(
        content = ByteReadChannel("""{"error":"MISSING_ACCESS_TOKEN"}"""),
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType to listOf("application/json")),
      )
    }
    val api = SessionApiImpl(client = buildClient(engine), baseUrl = baseUrl)

    val result = api.exchange(accessToken = "", refreshToken = "re")

    assertTrue(result.isFailure)
    assertIs<SessionExchangeError.MissingAccessToken>(result.exceptionOrNull())
  }

  @Test
  fun `400 missing refresh token maps to MissingRefreshToken`() = runTest {
    val engine = MockEngine { _ ->
      respond(
        content = ByteReadChannel("""{"error":"MISSING_REFRESH_TOKEN"}"""),
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType to listOf("application/json")),
      )
    }
    val api = SessionApiImpl(client = buildClient(engine), baseUrl = baseUrl)

    val result = api.exchange(accessToken = "ac", refreshToken = "")

    assertTrue(result.isFailure)
    assertIs<SessionExchangeError.MissingRefreshToken>(result.exceptionOrNull())
  }

  @Test
  fun `400 invalid json maps to InvalidJson`() = runTest {
    val engine = MockEngine { _ ->
      respond(
        content = ByteReadChannel("""{"error":"INVALID_JSON"}"""),
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType to listOf("application/json")),
      )
    }
    val api = SessionApiImpl(client = buildClient(engine), baseUrl = baseUrl)

    val result = api.exchange(accessToken = "ac", refreshToken = "re")

    assertTrue(result.isFailure)
    assertIs<SessionExchangeError.InvalidJson>(result.exceptionOrNull())
  }

  @Test
  fun `200 with set-cookie missing domain falls back to base url host`() = runTest {
    val engine = MockEngine { _ ->
      respond(
        content = ByteReadChannel("""{"ok":true,"isPortalLinked":false}"""),
        status = HttpStatusCode.OK,
        headers = headersOf(
          HttpHeaders.ContentType to listOf("application/json"),
          HttpHeaders.SetCookie to listOf(
            "cchaksa_session=abc; Path=/; Secure; HttpOnly; SameSite=Lax",
          ),
        ),
      )
    }
    val api = SessionApiImpl(client = buildClient(engine), baseUrl = baseUrl)

    val result = api.exchange(accessToken = "ac", refreshToken = "re")

    assertTrue(result.isSuccess)
    val cookie = result.getOrThrow().cookies.first()
    assertEquals("dv.cchaksa.com", cookie.domain)
    assertEquals("cchaksa_session", cookie.name)
    assertEquals("abc", cookie.value)
  }

  @Test
  fun `5xx maps to Unknown with httpStatus`() = runTest {
    val engine = MockEngine { _ ->
      respond(
        content = ByteReadChannel(""),
        status = HttpStatusCode.InternalServerError,
      )
    }
    val api = SessionApiImpl(client = buildClient(engine), baseUrl = baseUrl)

    val result = api.exchange(accessToken = "ac", refreshToken = "re")

    assertTrue(result.isFailure)
    val error = result.exceptionOrNull()
    assertIs<SessionExchangeError.Unknown>(error)
    assertEquals(500, error.httpStatus)
  }
}
