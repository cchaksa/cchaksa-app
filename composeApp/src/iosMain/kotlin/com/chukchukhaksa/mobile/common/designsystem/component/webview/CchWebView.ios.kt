package com.chukchukhaksa.mobile.common.designsystem.component.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import io.github.aakira.napier.Napier
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSHTTPCookieDomain
import platform.Foundation.NSHTTPCookieExpires
import platform.Foundation.NSHTTPCookieName
import platform.Foundation.NSHTTPCookiePath
import platform.Foundation.NSHTTPCookieSecure
import platform.Foundation.NSHTTPCookieValue
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.UIKit.UIRectEdgeLeft
import platform.UIKit.UIScreenEdgePanGestureRecognizer
import platform.UIKit.UIView
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebsiteDataStore
import platform.darwin.NSObject

private const val BRIDGE_HANDLER_NAME = "bridge"

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, ExperimentalNativeApi::class)
@Composable
actual fun CchWebView(
  url: String,
  controller: CchWebViewController,
  modifier: Modifier,
  cookies: List<WebViewCookie>,
  onBridgeMessage: (BridgeMessage) -> Unit,
) {
  val currentBridgeMessage = rememberUpdatedState(onBridgeMessage)
  val scriptMessageHandler = remember {
    BridgeScriptMessageHandler { message -> currentBridgeMessage.value(message) }
  }
  val updateCanGoBack = remember<(WKWebView) -> Unit> {
    { w ->
      val canGoBack = w.canGoBack
      controller.canGoBack = canGoBack
      setComposeEdgeGestureEnabled(w, enabled = !canGoBack)
    }
  }
  // SPA의 pushState/replaceState 이동은 내비게이션 델리게이트 콜백이 호출되지 않으므로
  // history API 후킹 스크립트로 변경을 통지받아 canGoBack을 갱신한다.
  val historyMessageHandler = remember { HistoryScriptMessageHandler(updateCanGoBack) }

  val webView = remember {
    val configuration = WKWebViewConfiguration().apply {
      userContentController = WKUserContentController().apply {
        addScriptMessageHandler(scriptMessageHandler, name = BRIDGE_HANDLER_NAME)
        addScriptMessageHandler(historyMessageHandler, name = HISTORY_OBSERVER_HANDLER_NAME)
        addUserScript(createHistoryObserverScript())
      }
      websiteDataStore = WKWebsiteDataStore.defaultDataStore()
    }
    WKWebView(
      frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
      configuration = configuration,
    ).apply {
      allowsBackForwardNavigationGestures = true
      customUserAgent = buildWebViewUserAgent("iOS")
      if (Platform.isDebugBinary) {
        setInspectable(true)
      }
    }
  }

  val navDelegate = remember {
    WebViewNavigationDelegate(
      onNavigationStateChanged = updateCanGoBack,
      onLoadingChanged = { loading -> controller.isLoading = loading },
    )
  }

  DisposableEffect(Unit) {
    webView.navigationDelegate = navDelegate
    val nsUrl = NSURL.URLWithString(url)
    val cookieStore = webView.configuration.websiteDataStore.httpCookieStore
    val nsCookies = cookies.mapNotNull { it.toNSHTTPCookie() }
    Napier.d(tag = "CchWebView") {
      "DisposableEffect: url=$url, cookies.size=${cookies.size}, nsCookies.size=${nsCookies.size}"
    }
    cookies.forEach { cookie ->
      Napier.d(tag = "CchWebView") { "  cookie [${cookie.toLogString()}]" }
    }
    if (nsCookies.isEmpty() || nsUrl == null) {
      Napier.d(tag = "CchWebView") {
        "No cookies to set or invalid url, calling loadRequest immediately (nsUrl=${nsUrl?.absoluteString})"
      }
      nsUrl?.let { webView.loadRequest(NSURLRequest(uRL = it)) }
    } else {
      var remaining = nsCookies.size
      nsCookies.forEach { nsCookie ->
        Napier.d(tag = "CchWebView") {
          "WKHTTPCookieStore.setCookie name=${nsCookie.name}, domain=${nsCookie.domain}, path=${nsCookie.path}"
        }
        cookieStore.setCookie(nsCookie) {
          remaining -= 1
          Napier.d(tag = "CchWebView") {
            "setCookie completion for ${nsCookie.name}, remaining=$remaining"
          }
          if (remaining == 0) {
            Napier.d(tag = "CchWebView") { "All cookies set → loadRequest($url)" }
            webView.loadRequest(NSURLRequest(uRL = nsUrl))
          }
        }
      }
    }
    controller.goBackAction = {
      if (webView.canGoBack) webView.goBack()
    }
    onDispose {
      setComposeEdgeGestureEnabled(webView, enabled = true)
      controller.goBackAction = null
      webView.navigationDelegate = null
      webView.configuration.userContentController.removeScriptMessageHandlerForName(BRIDGE_HANDLER_NAME)
      webView.configuration.userContentController.removeScriptMessageHandlerForName(HISTORY_OBSERVER_HANDLER_NAME)
    }
  }

  LaunchedEffect(cookies) {
    if (cookies.isEmpty()) {
      Napier.d(tag = "CchWebView") { "LaunchedEffect(cookies) skipped (empty)" }
      return@LaunchedEffect
    }
    val cookieStore = webView.configuration.websiteDataStore.httpCookieStore
    val nsCookies = cookies.mapNotNull { it.toNSHTTPCookie() }
    if (nsCookies.isEmpty()) return@LaunchedEffect
    Napier.d(tag = "CchWebView") {
      "LaunchedEffect(cookies) → re-injecting ${nsCookies.size} cookies, then reload()"
    }
    var remaining = nsCookies.size
    nsCookies.forEach { nsCookie ->
      cookieStore.setCookie(nsCookie) {
        remaining -= 1
        Napier.d(tag = "CchWebView") {
          "re-inject completion for ${nsCookie.name}, remaining=$remaining"
        }
        if (remaining == 0) {
          Napier.d(tag = "CchWebView") { "All cookies re-injected → reload()" }
          webView.reload()
        }
      }
    }
  }

  UIKitView(
    modifier = modifier,
    factory = { webView },
    update = {},
  )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun WebViewCookie.toNSHTTPCookie(): NSHTTPCookie? {
  val properties = mutableMapOf<Any?, Any?>(
    NSHTTPCookieName to name,
    NSHTTPCookieValue to value,
    NSHTTPCookieDomain to domain,
    NSHTTPCookiePath to path,
  )
  if (secure) {
    properties[NSHTTPCookieSecure] = "TRUE"
  }
  expiresEpochSeconds?.let { seconds ->
    properties[NSHTTPCookieExpires] = NSDate.dateWithTimeIntervalSince1970(seconds.toDouble())
  }
  return NSHTTPCookie.cookieWithProperties(properties as Map<Any?, *>)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class BridgeScriptMessageHandler(
  private val onBridgeMessage: (BridgeMessage) -> Unit,
) : NSObject(), WKScriptMessageHandlerProtocol {
  override fun userContentController(
    userContentController: WKUserContentController,
    didReceiveScriptMessage: WKScriptMessage,
  ) {
    val body = didReceiveScriptMessage.body as? String ?: return
    onBridgeMessage(BridgeMessage.parse(body))
  }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class WebViewNavigationDelegate(
  private val onNavigationStateChanged: (WKWebView) -> Unit,
  private val onLoadingChanged: (Boolean) -> Unit,
) : NSObject(), WKNavigationDelegateProtocol {

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
    Napier.d(tag = "CchWebView") { "didStartProvisionalNavigation url=${webView.URL?.absoluteString}" }
    onLoadingChanged(true)
    onNavigationStateChanged(webView)
  }

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didCommitNavigation: WKNavigation?) {
    Napier.d(tag = "CchWebView") { "didCommitNavigation url=${webView.URL?.absoluteString}" }
    onNavigationStateChanged(webView)
  }

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
    Napier.d(tag = "CchWebView") {
      "didFinishNavigation url=${webView.URL?.absoluteString}, canGoBack=${webView.canGoBack}"
    }
    onLoadingChanged(false)
    onNavigationStateChanged(webView)
  }

  @ObjCSignatureOverride
  override fun webView(
    webView: WKWebView,
    didFailNavigation: WKNavigation?,
    withError: NSError,
  ) {
    Napier.w(tag = "CchWebView") {
      "didFailNavigation url=${webView.URL?.absoluteString}, error=${withError.localizedDescription}"
    }
    onLoadingChanged(false)
    onNavigationStateChanged(webView)
  }

  @ObjCSignatureOverride
  override fun webView(
    webView: WKWebView,
    didFailProvisionalNavigation: WKNavigation?,
    withError: NSError,
  ) {
    Napier.w(tag = "CchWebView") {
      "didFailProvisionalNavigation url=${webView.URL?.absoluteString}, error=${withError.localizedDescription}"
    }
    onLoadingChanged(false)
    onNavigationStateChanged(webView)
  }
}

@OptIn(ExperimentalForeignApi::class)
internal fun setComposeEdgeGestureEnabled(webView: WKWebView, enabled: Boolean) {
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
