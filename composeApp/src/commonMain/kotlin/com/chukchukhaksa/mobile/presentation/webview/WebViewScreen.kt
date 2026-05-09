package com.chukchukhaksa.mobile.presentation.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import com.chukchukhaksa.mobile.common.designsystem.component.webview.CchWebView
import com.chukchukhaksa.mobile.common.designsystem.component.webview.CchWebViewController
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.common.designsystem.component.webview.rememberCchWebViewController
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.PlatformBackHandler
import com.chukchukhaksa.mobile.domain.webview.ExchangeWebSessionUseCase
import io.github.aakira.napier.Napier
import org.koin.compose.koinInject

@Composable
fun WebViewRoute(
  url: String,
  popBackStack: () -> Unit,
  onNavigateWebView: (String) -> Unit,
) {
  val controller = rememberCchWebViewController()
  val exchangeWebSession: ExchangeWebSessionUseCase = koinInject()
  val cookies by exchangeWebSession.cookies.collectAsStateWithLifecycle()

  WebViewRouteContent(
    url = url,
    cookies = cookies,
    popBackStack = popBackStack,
    controller = controller,
    onNavigateWebView = onNavigateWebView,
  )
}

@Composable
private fun WebViewRouteContent(
  url: String,
  cookies: List<WebViewCookie>,
  popBackStack: () -> Unit,
  controller: CchWebViewController,
  onNavigateWebView: (String) -> Unit,
) {
  PlatformBackHandler(enabled = controller.canGoBack) {
    controller.goBack()
  }
  val currentHost = remember(url) {
    url.substringAfter("://").substringBefore("/")
  }
  var lastPushedUrl by rememberSaveable { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(White100)
      .windowInsetsPadding(WindowInsets.systemBars),
  ) {
    CchWebView(
      url = url,
      controller = controller,
      cookies = cookies,
      onBridgeMessage = { message: BridgeMessage ->
        Napier.w(tag = "BridgeAction") { "raw message: $message" }
        when (val action = message.toAction(currentHost)) {
          is BridgeAction.NavigateWebView -> {
            if (lastPushedUrl == action.absoluteUrl) {
              Napier.w(tag = "BridgeAction") { "Skipped duplicate push: ${action.absoluteUrl}" }
            } else {
              lastPushedUrl = action.absoluteUrl
              onNavigateWebView(action.absoluteUrl)
            }
          }

          is BridgeAction.Unhandled -> Unit
        }
      },
      modifier = Modifier.fillMaxSize(),
    )
  }
}
