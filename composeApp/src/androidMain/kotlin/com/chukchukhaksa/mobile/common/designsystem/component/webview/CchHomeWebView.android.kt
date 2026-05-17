package com.chukchukhaksa.mobile.common.designsystem.component.webview

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

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
    holder.canGoBackListener = { canGoBack -> controller.canGoBack = canGoBack }
    controller.goBackAction = {
      val view = holder.webView
      if (view.canGoBack()) view.goBack()
    }
    controller.canGoBack = holder.webView.canGoBack()
    onDispose {
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

  AndroidView(
    modifier = modifier,
    factory = { _ ->
      val view = holder.webView
      (view.parent as? ViewGroup)?.removeView(view)
      view.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
      )
      view
    },
    onRelease = { view ->
      (view.parent as? ViewGroup)?.removeView(view)
    },
  )
}
