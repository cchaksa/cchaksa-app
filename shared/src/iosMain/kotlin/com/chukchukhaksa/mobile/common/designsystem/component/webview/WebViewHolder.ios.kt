package com.chukchukhaksa.mobile.common.designsystem.component.webview

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
import platform.WebKit.WKAudiovisualMediaTypeNone
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
actual class WebViewHolder {

  internal var bridgeMessageListener: ((BridgeMessage) -> Unit)? = null
  internal var canGoBackListener: ((Boolean) -> Unit)? = null

  private var hasLoadedInitial = false

  private val scriptHandler = HolderBridgeScriptMessageHandler { msg ->
    bridgeMessageListener?.invoke(msg)
  }

  private val navDelegate = HolderNavigationDelegate { canGoBack ->
    canGoBackListener?.invoke(canGoBack)
  }

  // SPA의 pushState/replaceState 이동은 델리게이트 콜백이 없으므로
  // history API 후킹 스크립트의 통지로 canGoBack을 보강 갱신한다.
  private val historyHandler = HistoryScriptMessageHandler { w ->
    canGoBackListener?.invoke(w.canGoBack)
  }

  internal val webView: WKWebView by lazy { createPersistentWebView() }

  actual fun preload(url: String, cookies: List<WebViewCookie>) {
    val nsUrl = NSURL.URLWithString(url) ?: run {
      Napier.w(tag = "WebViewHolder") { "preload: invalid url=$url" }
      return
    }
    if (hasLoadedInitial) {
      Napier.d(tag = "WebViewHolder") { "preload skipped: already loaded; cookies sync only" }
      syncCookies(cookies) {}
      return
    }
    Napier.d(tag = "WebViewHolder") { "preload: url=$url, cookies.size=${cookies.size}" }
    syncCookies(cookies) {
      Napier.d(tag = "WebViewHolder") { "cookies synced → loadRequest($url)" }
      webView.loadRequest(NSURLRequest(uRL = nsUrl))
    }
    hasLoadedInitial = true
  }

  actual fun updateCookies(cookies: List<WebViewCookie>) {
    syncCookies(cookies) {}
  }

  actual fun isInitialLoaded(): Boolean = hasLoadedInitial

  actual fun reset() {
    Napier.d(tag = "WebViewHolder") { "reset() called → next preload will reload url" }
    hasLoadedInitial = false
  }

  private fun createPersistentWebView(): WKWebView {
    val configuration = WKWebViewConfiguration().apply {
      userContentController = WKUserContentController().apply {
        addScriptMessageHandler(scriptHandler, name = BRIDGE_HANDLER_NAME)
        addScriptMessageHandler(historyHandler, name = HISTORY_OBSERVER_HANDLER_NAME)
        addUserScript(createHistoryObserverScript())
      }
      websiteDataStore = WKWebsiteDataStore.defaultDataStore()
      // 인라인(비전체화면) 동영상 재생 허용 — iOS(iPhone) 기본값 false
      allowsInlineMediaPlayback = true
      // 사용자 제스처 없이 자동재생 허용 — muted 여부는 프론트의 <video> 속성으로 제어
      mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone
    }
    return WKWebView(
      frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
      configuration = configuration,
    ).apply {
      allowsBackForwardNavigationGestures = true
      customUserAgent = buildWebViewUserAgent("iOS")
      navigationDelegate = navDelegate
      if (Platform.isDebugBinary) {
        setInspectable(true)
      }
    }
  }

  private fun syncCookies(cookies: List<WebViewCookie>, onComplete: () -> Unit) {
    if (cookies.isEmpty()) {
      Napier.d(tag = "WebViewHolder") { "syncCookies skipped (empty list)" }
      onComplete()
      return
    }
    val cookieStore = webView.configuration.websiteDataStore.httpCookieStore
    val nsCookies = cookies.mapNotNull { it.toNSHTTPCookie() }
    if (nsCookies.isEmpty()) {
      onComplete()
      return
    }
    var remaining = nsCookies.size
    nsCookies.forEach { nsCookie ->
      Napier.d(tag = "WebViewHolder") {
        "WKHTTPCookieStore.setCookie name=${nsCookie.name}, domain=${nsCookie.domain}, path=${nsCookie.path}"
      }
      cookieStore.setCookie(nsCookie) {
        remaining -= 1
        if (remaining == 0) {
          onComplete()
        }
      }
    }
  }
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
private class HolderBridgeScriptMessageHandler(
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
private class HolderNavigationDelegate(
  private val onCanGoBackChanged: (Boolean) -> Unit,
) : NSObject(), WKNavigationDelegateProtocol {

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
    Napier.d(tag = "WebViewHolder") { "didStartProvisionalNavigation url=${webView.URL?.absoluteString}" }
    onCanGoBackChanged(webView.canGoBack)
  }

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didCommitNavigation: WKNavigation?) {
    Napier.d(tag = "WebViewHolder") { "didCommitNavigation url=${webView.URL?.absoluteString}" }
    onCanGoBackChanged(webView.canGoBack)
  }

  @ObjCSignatureOverride
  override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
    Napier.d(tag = "WebViewHolder") {
      "didFinishNavigation url=${webView.URL?.absoluteString}, canGoBack=${webView.canGoBack}"
    }
    onCanGoBackChanged(webView.canGoBack)
  }

  @ObjCSignatureOverride
  override fun webView(
    webView: WKWebView,
    didFailNavigation: WKNavigation?,
    withError: NSError,
  ) {
    Napier.w(tag = "WebViewHolder") {
      "didFailNavigation url=${webView.URL?.absoluteString}, error=${withError.localizedDescription}"
    }
    onCanGoBackChanged(webView.canGoBack)
  }

  @ObjCSignatureOverride
  override fun webView(
    webView: WKWebView,
    didFailProvisionalNavigation: WKNavigation?,
    withError: NSError,
  ) {
    Napier.w(tag = "WebViewHolder") {
      "didFailProvisionalNavigation url=${webView.URL?.absoluteString}, error=${withError.localizedDescription}"
    }
    onCanGoBackChanged(webView.canGoBack)
  }
}
