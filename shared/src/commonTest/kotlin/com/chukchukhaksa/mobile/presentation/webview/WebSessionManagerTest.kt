package com.chukchukhaksa.mobile.presentation.webview

import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.remote.auth.FakeLocalAuthDataSource
import com.chukchukhaksa.mobile.remote.auth.SessionApi
import com.chukchukhaksa.mobile.remote.auth.SessionExchangeError
import com.chukchukhaksa.mobile.remote.auth.SessionExchangeResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WebSessionManagerTest {

  private class FakeSessionApi(
    private val gate: CompletableDeferred<Unit>? = null,
    private val response: () -> Result<SessionExchangeResult>,
  ) : SessionApi {
    var callCount: Int = 0
      private set

    override suspend fun exchange(
      accessToken: String,
      refreshToken: String,
    ): Result<SessionExchangeResult> {
      callCount += 1
      gate?.await()
      return response()
    }
  }

  @Test
  fun `missing tokens returns NotLoggedIn without API call`() = runTest {
    val auth = FakeLocalAuthDataSource()
    val api = FakeSessionApi { Result.success(SessionExchangeResult(emptyList())) }
    val webSessionManager = WebSessionManager(auth, api)

    val status = webSessionManager.refresh()

    assertIs<ExchangeStatus.NotLoggedIn>(status)
    assertEquals(0, api.callCount)
  }

  @Test
  fun `success caches cookies and Loaded status`() = runTest {
    val auth = FakeLocalAuthDataSource(initialAccessToken = "ac", initialRefreshToken = "re")
    val cookies = listOf(
      WebViewCookie(name = "cchaksa_session", value = "abc", domain = ".cchaksa.com"),
    )
    val api = FakeSessionApi {
      Result.success(SessionExchangeResult(cookies = cookies))
    }
    val webSessionManager = WebSessionManager(auth, api)

    val first = webSessionManager.refresh()
    val second = webSessionManager.refresh()

    assertIs<ExchangeStatus.Loaded>(first)
    assertIs<ExchangeStatus.Loaded>(second)
    assertEquals(1, api.callCount)
  }

  @Test
  fun `400 maps to Failed400 and clears cache`() = runTest {
    val auth = FakeLocalAuthDataSource(initialAccessToken = "ac", initialRefreshToken = "re")
    val cookies = listOf(
      WebViewCookie(name = "cchaksa_session", value = "abc", domain = ".cchaksa.com"),
    )
    var fail = false
    val api = FakeSessionApi {
      if (fail) {
        Result.failure(SessionExchangeError.MissingAccessToken)
      } else {
        Result.success(SessionExchangeResult(cookies = cookies))
      }
    }
    val webSessionManager = WebSessionManager(auth, api)
    webSessionManager.refresh()
    auth.saveAccessToken("ac2")
    fail = true

    val status = webSessionManager.refresh()

    assertIs<ExchangeStatus.Failed400>(status)
    assertTrue(webSessionManager.cookies.value.isEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `concurrent refresh calls share one exchange`() = runTest {
    val auth = FakeLocalAuthDataSource(initialAccessToken = "ac", initialRefreshToken = "re")
    val cookies = listOf(
      WebViewCookie(name = "cchaksa_session", value = "abc", domain = ".cchaksa.com"),
    )
    val gate = CompletableDeferred<Unit>()
    val api = FakeSessionApi(gate = gate) {
      Result.success(SessionExchangeResult(cookies = cookies))
    }
    val webSessionManager = WebSessionManager(auth, api)

    val first = async { webSessionManager.refresh() }
    val second = async { webSessionManager.refresh() }
    runCurrent()
    gate.complete(Unit)

    assertIs<ExchangeStatus.Loaded>(first.await())
    assertIs<ExchangeStatus.Loaded>(second.await())
    assertEquals(1, api.callCount)
  }

  @Test
  fun `5xx maps to Failed5xx`() = runTest {
    val auth = FakeLocalAuthDataSource(initialAccessToken = "ac", initialRefreshToken = "re")
    val api = FakeSessionApi {
      Result.failure(SessionExchangeError.Unknown(httpStatus = 503, throwable = null))
    }
    val webSessionManager = WebSessionManager(auth, api)

    val status = webSessionManager.refresh()

    assertIs<ExchangeStatus.Failed5xx>(status)
  }

  @Test
  fun `clear resets cache`() = runTest {
    val auth = FakeLocalAuthDataSource(initialAccessToken = "ac", initialRefreshToken = "re")
    val cookies = listOf(
      WebViewCookie(name = "cchaksa_session", value = "abc", domain = ".cchaksa.com"),
    )
    val api = FakeSessionApi {
      Result.success(SessionExchangeResult(cookies = cookies))
    }
    val webSessionManager = WebSessionManager(auth, api)
    webSessionManager.refresh()
    webSessionManager.clear()

    val status = webSessionManager.refresh()

    assertTrue(status is ExchangeStatus.Loaded)
    assertEquals(2, api.callCount)
  }
}
