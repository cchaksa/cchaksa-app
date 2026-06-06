package com.chukchukhaksa.mobile.domain.webview

import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.remote.auth.FakeLocalAuthDataSource
import com.chukchukhaksa.mobile.remote.auth.SessionApi
import com.chukchukhaksa.mobile.remote.auth.SessionExchangeError
import com.chukchukhaksa.mobile.remote.auth.SessionExchangeResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ExchangeWebSessionUseCaseTest {

  private class FakeSessionApi(
    private val response: () -> Result<SessionExchangeResult>,
  ) : SessionApi {
    var callCount: Int = 0
      private set

    override suspend fun exchange(
      accessToken: String,
      refreshToken: String,
    ): Result<SessionExchangeResult> {
      callCount += 1
      return response()
    }
  }

  @Test
  fun `missing tokens returns NotLoggedIn without API call`() = runTest {
    val auth = FakeLocalAuthDataSource()
    val api = FakeSessionApi { Result.success(SessionExchangeResult(emptyList())) }
    val useCase = ExchangeWebSessionUseCase(auth, api)

    val status = useCase.refresh()

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
    val useCase = ExchangeWebSessionUseCase(auth, api)

    val first = useCase.refresh()
    val second = useCase.refresh()

    assertIs<ExchangeStatus.Loaded>(first)
    assertIs<ExchangeStatus.Loaded>(second)
    assertEquals(1, api.callCount)
  }

  @Test
  fun `400 maps to Failed400 and clears cache`() = runTest {
    val auth = FakeLocalAuthDataSource(initialAccessToken = "ac", initialRefreshToken = "re")
    val api = FakeSessionApi { Result.failure(SessionExchangeError.MissingAccessToken) }
    val useCase = ExchangeWebSessionUseCase(auth, api)

    val status = useCase.refresh()

    assertIs<ExchangeStatus.Failed400>(status)
  }

  @Test
  fun `5xx maps to Failed5xx`() = runTest {
    val auth = FakeLocalAuthDataSource(initialAccessToken = "ac", initialRefreshToken = "re")
    val api = FakeSessionApi {
      Result.failure(SessionExchangeError.Unknown(httpStatus = 503, throwable = null))
    }
    val useCase = ExchangeWebSessionUseCase(auth, api)

    val status = useCase.refresh()

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
    val useCase = ExchangeWebSessionUseCase(auth, api)
    useCase.refresh()
    useCase.clear()

    val status = useCase.refresh()

    assertTrue(status is ExchangeStatus.Loaded)
    assertEquals(2, api.callCount)
  }
}
