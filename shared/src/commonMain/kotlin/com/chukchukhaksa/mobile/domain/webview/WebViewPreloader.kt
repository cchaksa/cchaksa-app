package com.chukchukhaksa.mobile.domain.webview

import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewHolder
import com.chukchukhaksa.mobile.common.designsystem.component.webview.webHomeUrl
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WebViewPreloader(
  private val exchangeWebSession: ExchangeWebSessionUseCase,
  private val webViewHolder: WebViewHolder,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  /**
   * 세션 쿠키 교환과 홈 WebView preload를 백그라운드에서 비동기로 수행한다.
   * 완료를 기다릴 필요가 없는 앱 시작 시점 등에서 사용한다.
   */
  fun preload() {
    scope.launch { preloadAndAwait() }
  }

  /**
   * 세션 쿠키 교환(setCookie 소스)과 홈 WebView preload가 끝날 때까지 suspend로 대기한다.
   * 로그인 직후처럼 쿠키가 준비된 뒤에 화면 전환을 해야 하는 경우에 사용한다.
   */
  suspend fun preloadAndAwait(): ExchangeStatus {
    Napier.d(tag = "WebViewPreloader") {
      "preload() requested (initialLoaded=${webViewHolder.isInitialLoaded()})"
    }
    val status = exchangeWebSession.refresh()
    Napier.d(tag = "WebViewPreloader") { "exchange status=$status" }
    if (status == ExchangeStatus.Loaded) {
      webViewHolder.preload(
        url = webHomeUrl,
        cookies = exchangeWebSession.cookies.value,
      )
    } else {
      Napier.w(tag = "WebViewPreloader") { "skipping preload because status=$status" }
    }
    return status
  }
}
