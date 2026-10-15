package com.chukchukhaksa.mobile.presentation.webview

import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.data.auth.datasource.LocalAuthDataSource
import com.chukchukhaksa.mobile.remote.auth.SessionApi
import com.chukchukhaksa.mobile.remote.auth.SessionExchangeError
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** 앱의 access/refresh token을 웹 세션 쿠키로 교환하고, 교환한 쿠키와 교환 상태를 보관한다. */
@OptIn(ExperimentalTime::class)
class WebSessionManager(
  private val localAuthDataSource: LocalAuthDataSource,
  private val sessionApi: SessionApi,
) {
  private val cache = MutableStateFlow<List<WebViewCookie>>(emptyList())
  private val _status = MutableStateFlow<ExchangeStatus>(ExchangeStatus.Loading)
  private var lastTokenPair: Pair<String, String>? = null

  // 앱 시작·홈 진입·홈 웹뷰가 거의 동시에 refresh()를 부른다. 교환을 한 번에 하나만 돌려
  // 뒤따른 호출이 캐시를 재사용하게 하고, 늦게 끝난 실패가 앞선 성공의 쿠키를 지우지 않게 한다.
  private val refreshMutex = Mutex()

  val cookies: StateFlow<List<WebViewCookie>> = cache.asStateFlow()
  val status: StateFlow<ExchangeStatus> = _status.asStateFlow()

  suspend fun refresh(): ExchangeStatus = refreshMutex.withLock { refreshLocked() }

  private suspend fun refreshLocked(): ExchangeStatus {
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

  fun hasExpiredCookies(nowEpochSeconds: Long = Clock.System.now().epochSeconds): Boolean {
    val cookies = cache.value
    if (cookies.isEmpty()) return false
    return cookies.any { cookie ->
      val expires = cookie.expiresEpochSeconds ?: return@any false
      expires <= nowEpochSeconds
    }
  }

  suspend fun refreshIfExpired(
    nowEpochSeconds: Long = Clock.System.now().epochSeconds,
  ): ExchangeStatus? {
    if (!hasExpiredCookies(nowEpochSeconds)) {
      Napier.d(tag = "ExchangeWebSession") { "refreshIfExpired() skipped (no expired cookies)" }
      return null
    }
    Napier.d(tag = "ExchangeWebSession") { "refreshIfExpired() expired cookies detected → forcing refresh" }
    return refreshMutex.withLock {
      // 기다리는 동안 다른 호출이 새 쿠키를 받아 왔으면 다시 교환하지 않는다.
      if (hasExpiredCookies(nowEpochSeconds)) lastTokenPair = null
      refreshLocked()
    }
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
