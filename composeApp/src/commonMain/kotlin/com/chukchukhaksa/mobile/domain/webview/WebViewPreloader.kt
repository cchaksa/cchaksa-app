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

  fun preload() {
    Napier.d(tag = "WebViewPreloader") {
      "preload() requested (initialLoaded=${webViewHolder.isInitialLoaded()})"
    }
    scope.launch {
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
    }
  }
}
