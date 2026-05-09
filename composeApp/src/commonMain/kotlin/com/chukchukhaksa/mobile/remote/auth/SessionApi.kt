package com.chukchukhaksa.mobile.remote.auth

import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie

interface SessionApi {
  suspend fun exchange(accessToken: String, refreshToken: String): Result<SessionExchangeResult>
}

data class SessionExchangeResult(
  val cookies: List<WebViewCookie>,
  val isPortalLinked: Boolean,
)

sealed class SessionExchangeError : Throwable() {
  data object MissingAccessToken : SessionExchangeError()
  data object MissingRefreshToken : SessionExchangeError()
  data object InvalidJson : SessionExchangeError()
  data class Unknown(val httpStatus: Int?, val throwable: Throwable?) : SessionExchangeError()
}
