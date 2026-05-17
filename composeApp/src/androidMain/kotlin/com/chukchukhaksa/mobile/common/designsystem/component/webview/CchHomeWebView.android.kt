package com.chukchukhaksa.mobile.common.designsystem.component.webview

import android.view.ViewGroup
import android.widget.FrameLayout
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
    factory = { context ->
      FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT,
        )
      }
    },
    update = { container ->
      val view = holder.webView
      val currentParent = view.parent as? ViewGroup
      if (currentParent !== container) {
        currentParent?.removeView(view)
        container.addView(
          view,
          FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
          ),
        )
      }
      view.requestLayout()
    },
    onRelease = { container ->
      container.removeAllViews()
    },
  )
}
