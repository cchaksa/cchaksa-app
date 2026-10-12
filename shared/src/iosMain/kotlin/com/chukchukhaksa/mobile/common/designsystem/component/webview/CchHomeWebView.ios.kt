package com.chukchukhaksa.mobile.common.designsystem.component.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CchHomeWebView(
  holder: WebViewHolder,
  controller: CchWebViewController,
  modifier: Modifier,
  cookies: List<WebViewCookie>,
  onBridgeMessage: (BridgeMessage) -> Unit,
) {
  val currentBridgeMessage = rememberUpdatedState(onBridgeMessage)

  DisposableEffect(holder) {
    holder.bridgeMessageListener = { msg -> currentBridgeMessage.value(msg) }
    holder.canGoBackListener = { canGoBack ->
      controller.canGoBack = canGoBack
      setComposeEdgeGestureEnabled(holder.webView, enabled = !canGoBack)
    }
    controller.goBackAction = {
      val view = holder.webView
      if (view.canGoBack) view.goBack()
    }
    val initialCanGoBack = holder.webView.canGoBack
    controller.canGoBack = initialCanGoBack
    setComposeEdgeGestureEnabled(holder.webView, enabled = !initialCanGoBack)
    onDispose {
      setComposeEdgeGestureEnabled(holder.webView, enabled = true)
      holder.bridgeMessageListener = null
      holder.canGoBackListener = null
      controller.goBackAction = null
    }
  }

  LaunchedEffect(cookies) {
    if (cookies.isNotEmpty()) {
      holder.updateCookies(cookies)
    }
  }

  UIKitView(
    modifier = modifier,
    factory = {
      holder.webView.removeFromSuperview()
      holder.webView
    },
    update = {},
  )
}
