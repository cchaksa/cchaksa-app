package com.chukchukhaksa.mobile.common.designsystem.component.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIRectEdgeLeft
import platform.UIKit.UIScreenEdgePanGestureRecognizer
import platform.UIKit.UIView
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun CchWebView(
  url: String,
  controller: CchWebViewController,
  modifier: Modifier,
) {
  val webView = remember {
    WKWebView(
      frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
      configuration = WKWebViewConfiguration(),
    ).apply {
      allowsBackForwardNavigationGestures = true
    }
  }

  val navDelegate = remember {
    WebViewNavigationDelegate { w ->
      val canGoBack = w.canGoBack
      controller.canGoBack = canGoBack
      setComposeEdgeGestureEnabled(w, enabled = !canGoBack)
    }
  }

  DisposableEffect(Unit) {
    webView.navigationDelegate = navDelegate
    NSURL.URLWithString(url)?.let { nsUrl ->
      webView.loadRequest(NSURLRequest(uRL = nsUrl))
    }
    controller.goBackAction = {
      if (webView.canGoBack) webView.goBack()
    }
    onDispose {
      setComposeEdgeGestureEnabled(webView, enabled = true)
      controller.goBackAction = null
      webView.navigationDelegate = null
    }
  }

  UIKitView(
    modifier = modifier,
    factory = { webView },
    update = {},
  )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class WebViewNavigationDelegate(
  private val onNavigationStateChanged: (WKWebView) -> Unit,
) : NSObject(), WKNavigationDelegateProtocol {

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
    onNavigationStateChanged(webView)
  }

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
    onNavigationStateChanged(webView)
  }

  @ObjCSignatureOverride
  override fun webView(
    webView: WKWebView,
    didFailNavigation: WKNavigation?,
    withError: NSError,
  ) {
    onNavigationStateChanged(webView)
  }

  @ObjCSignatureOverride
  override fun webView(
    webView: WKWebView,
    didFailProvisionalNavigation: WKNavigation?,
    withError: NSError,
  ) {
    onNavigationStateChanged(webView)
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun setComposeEdgeGestureEnabled(webView: WKWebView, enabled: Boolean) {
  var view: UIView? = webView.superview
  while (view != null) {
    view.gestureRecognizers?.forEach { recognizer ->
      val edgeRecognizer = recognizer as? UIScreenEdgePanGestureRecognizer
      if (edgeRecognizer != null && edgeRecognizer.edges == UIRectEdgeLeft) {
        edgeRecognizer.setEnabled(enabled)
      }
    }
    view = view.superview
  }
}
