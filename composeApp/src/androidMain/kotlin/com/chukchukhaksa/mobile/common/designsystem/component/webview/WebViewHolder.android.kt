package com.chukchukhaksa.mobile.common.designsystem.component.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import io.github.aakira.napier.Napier

@SuppressLint("SetJavaScriptEnabled")
actual class WebViewHolder(private val context: Context) {

  internal var bridgeMessageListener: ((BridgeMessage) -> Unit)? = null
  internal var canGoBackListener: ((Boolean) -> Unit)? = null

  private val mainHandler = Handler(Looper.getMainLooper())
  private var hasLoadedInitial = false

  internal val webView: WebView by lazy { createPersistentWebView() }

  actual fun preload(url: String, cookies: List<WebViewCookie>) {
    if (hasLoadedInitial) {
      Napier.d(tag = "WebViewHolder") { "preload skipped: already loaded; cookies sync only" }
      syncCookies(cookies)
      return
    }
    Napier.d(tag = "WebViewHolder") { "preload: url=$url, cookies.size=${cookies.size}" }
    syncCookies(cookies)
    webView.loadUrl(url)
    hasLoadedInitial = true
  }

  actual fun updateCookies(cookies: List<WebViewCookie>) {
    syncCookies(cookies)
  }

  actual fun isInitialLoaded(): Boolean = hasLoadedInitial

  actual fun reset() {
    Napier.d(tag = "WebViewHolder") { "reset() called → next preload will reload url" }
    hasLoadedInitial = false
  }

  private fun createPersistentWebView(): WebView = WebView(context.applicationContext).apply {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.useWideViewPort = true
    settings.loadWithOverviewMode = true
    settings.textZoom = 100
    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
    settings.cacheMode = WebSettings.LOAD_DEFAULT
    settings.userAgentString = buildWebViewUserAgent("Android")
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false
    scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    setLayerType(View.LAYER_TYPE_HARDWARE, null)
    addJavascriptInterface(
      HolderJsBridge { message ->
        mainHandler.post { bridgeMessageListener?.invoke(message) }
      },
      BRIDGE_INTERFACE_NAME,
    )
    webChromeClient = object : WebChromeClient() {
      override fun onConsoleMessage(message: ConsoleMessage): Boolean {
        Napier.d(tag = "WebConsole") {
          "[${message.messageLevel()}] ${message.message()} @ ${message.sourceId()}:${message.lineNumber()}"
        }
        return true
      }
    }
    webViewClient = object : WebViewClient() {
      override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Napier.d(tag = "WebViewHolder") { "onPageStarted url=$url" }
      }

      override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Napier.d(tag = "WebViewHolder") { "onPageFinished url=$url" }
      }

      override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        val canGoBack = view?.canGoBack() == true
        Napier.d(tag = "WebViewHolder") {
          "doUpdateVisitedHistory url=$url, isReload=$isReload, canGoBack=$canGoBack"
        }
        canGoBackListener?.invoke(canGoBack)
      }
    }
  }
}

private const val BRIDGE_INTERFACE_NAME = "Android"

private fun syncCookies(cookies: List<WebViewCookie>) {
  if (cookies.isEmpty()) {
    Napier.d(tag = "WebViewHolder") { "syncCookies skipped (empty list)" }
    return
  }
  val manager = CookieManager.getInstance()
  manager.setAcceptCookie(true)
  cookies.forEach { cookie ->
    if (cookie.domain.isBlank()) {
      Napier.w(tag = "WebViewHolder") {
        "syncCookies: domain is blank for ${cookie.name}, skipping"
      }
      return@forEach
    }
    val url = buildCookieUrl(cookie)
    val serialized = serializeCookie(cookie)
    Napier.d(tag = "WebViewHolder") {
      "CookieManager.setCookie url=$url, cookie=[${cookie.toLogString()}]"
    }
    manager.setCookie(url, serialized) { ok ->
      Napier.d(tag = "WebViewHolder") {
        "setCookie callback ok=$ok for ${cookie.name}@${cookie.domain}"
      }
    }
  }
  manager.flush()
}

private fun buildCookieUrl(cookie: WebViewCookie): String {
  val host = cookie.domain.trimStart('.')
  val scheme = if (cookie.secure) "https" else "http"
  return "$scheme://$host${cookie.path}"
}

private fun serializeCookie(cookie: WebViewCookie): String = buildString {
  append("${cookie.name}=${cookie.value}")
  if (cookie.domain.isNotBlank()) append("; Domain=${cookie.domain}")
  append("; Path=${cookie.path}")
  if (cookie.secure) append("; Secure")
  if (cookie.httpOnly) append("; HttpOnly")
  append("; SameSite=${cookie.sameSite}")
}

private class HolderJsBridge(
  private val onBridgeMessage: (BridgeMessage) -> Unit,
) {
  @JavascriptInterface
  fun postMessage(message: String) {
    val parsed = BridgeMessage.parse(message)
    onBridgeMessage(parsed)
  }
}
