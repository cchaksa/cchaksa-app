package com.chukchukhaksa.mobile.domain.webview

import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.data.auth.datasource.LocalAuthDataSource
import com.chukchukhaksa.mobile.remote.auth.SessionApi
import com.chukchukhaksa.mobile.remote.auth.SessionExchangeError
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExchangeWebSessionUseCase(
  private val localAuthDataSource: LocalAuthDataSource,
  private val sessionApi: SessionApi,
) {
  private val cache = MutableStateFlow<List<WebViewCookie>>(emptyList())
  private val _status = MutableStateFlow<ExchangeStatus>(ExchangeStatus.Loading)
  private var lastTokenPair: Pair<String, String>? = null

  val cookies: StateFlow<List<WebViewCookie>> = cache.asStateFlow()
  val status: StateFlow<ExchangeStatus> = _status.asStateFlow()

  suspend fun refresh(): ExchangeStatus {
    Napier.d(tag = "ExchangeWebSession") { "refresh() called" }
    val ac = localAuthDataSource.getAccessToken()
    val re = localAuthDataSource.getRefreshToken()
    if (ac.isNullOrEmpty() || re.isNullOrEmpty()) {
      Napier.d(tag = "ExchangeWebSession") {
        "Tokens missing (access=${!ac.isNullOrEmpty()}, refresh=${!re.isNullOrEmpty()}) → NotLoggedIn"
      }
      cache.value = emptyList()
      lastTokenPair = null
      _status.value = ExchangeStatus.NotLoggedIn
      return ExchangeStatus.NotLoggedIn
    }
    if (lastTokenPair == ac to re && cache.value.isNotEmpty()) {
      Napier.d(tag = "ExchangeWebSession") {
        "Cache hit (cookies.size=${cache.value.size}) → Loaded"
      }
      _status.value = ExchangeStatus.Loaded
      return ExchangeStatus.Loaded
    }
    Napier.d(tag = "ExchangeWebSession") { "Cache miss → calling SessionApi.exchange" }
    _status.value = ExchangeStatus.Loading
    val result = sessionApi.exchange(ac, re)
    val nextStatus = result.fold(
      onSuccess = { exchange ->
        Napier.d(tag = "ExchangeWebSession") {
          "Exchange success (cookies.size=${exchange.cookies.size}) → cache updated"
        }
        cache.value = exchange.cookies
        lastTokenPair = if (exchange.cookies.isNotEmpty()) ac to re else null
        ExchangeStatus.Loaded
      },
      onFailure = { error ->
        Napier.w(tag = "ExchangeWebSession") { "Exchange failed: $error" }
        cache.value = emptyList()
        lastTokenPair = null
        when (error) {
          is SessionExchangeError.MissingAccessToken,
          is SessionExchangeError.MissingRefreshToken,
          is SessionExchangeError.InvalidJson,
          -> ExchangeStatus.Failed400

          is SessionExchangeError.Unknown -> ExchangeStatus.Failed5xx
          else -> ExchangeStatus.Failed5xx
        }
      },
    )
    Napier.d(tag = "ExchangeWebSession") { "refresh() returning status=$nextStatus" }
    _status.value = nextStatus
    return nextStatus
  }

  fun clear() {
    Napier.d(tag = "ExchangeWebSession") { "clear() called → cache emptied" }
    cache.value = emptyList()
    lastTokenPair = null
    _status.value = ExchangeStatus.NotLoggedIn
  }
}

sealed interface ExchangeStatus {
  data object Loading : ExchangeStatus
  data object Loaded : ExchangeStatus
  data object NotLoggedIn : ExchangeStatus
  data object Failed400 : ExchangeStatus
  data object Failed5xx : ExchangeStatus
}
