package com.chukchukhaksa.mobile.common.designsystem.component.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.chukchukhaksa.mobile.common.designsystem.component.webview.toLogString
import io.github.aakira.napier.Napier

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun CchWebView(
  url: String,
  controller: CchWebViewController,
  modifier: Modifier,
  cookies: List<WebViewCookie>,
  onBridgeMessage: (BridgeMessage) -> Unit,
) {
  val webView = remember {
    object : Any() {
      var instance: WebView? = null
    }
  }
  val currentBridgeMessage = rememberUpdatedState(onBridgeMessage)

  DisposableEffect(controller) {
    controller.goBackAction = {
      webView.instance?.let { if (it.canGoBack()) it.goBack() }
    }
    onDispose {
      controller.goBackAction = null
    }
  }

  LaunchedEffect(cookies, url) {
    syncCookies(cookies)
  }

  AndroidView(
    modifier = modifier,
    factory = { context ->
      WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT,
        )
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
          JsBridge { message -> currentBridgeMessage.value(message) },
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
            Napier.d(tag = "CchWebView") { "onPageStarted url=$url" }
          }

          override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Napier.d(tag = "CchWebView") { "onPageFinished url=$url" }
          }

          override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            Napier.d(tag = "CchWebView") {
              "doUpdateVisitedHistory url=$url, isReload=$isReload, canGoBack=${view?.canGoBack() == true}"
            }
            controller.canGoBack = view?.canGoBack() == true
          }
        }
        webView.instance = this
        syncCookies(cookies)
        loadUrl(url)
      }
    },
    onRelease = { view ->
      view.removeJavascriptInterface(BRIDGE_INTERFACE_NAME)
      webView.instance = null
      view.destroy()
    },
  )
}

private const val BRIDGE_INTERFACE_NAME = "Android"

private fun syncCookies(cookies: List<WebViewCookie>) {
  if (cookies.isEmpty()) {
    Napier.d(tag = "CchWebView") { "syncCookies skipped (empty list)" }
    return
  }
  val manager = CookieManager.getInstance()
  manager.setAcceptCookie(true)
  cookies.forEach { cookie ->
    if (cookie.domain.isBlank()) {
      Napier.w(tag = "CchWebView") {
        "syncCookies: domain is blank for ${cookie.name}, skipping"
      }
      return@forEach
    }
    val url = buildCookieUrl(cookie)
    val serialized = serializeCookie(cookie)
    Napier.d(tag = "CchWebView") {
      "CookieManager.setCookie url=$url, cookie=[${cookie.toLogString()}]"
    }
    manager.setCookie(url, serialized) { ok ->
      Napier.d(tag = "CchWebView") {
        "setCookie callback ok=$ok for ${cookie.name}@${cookie.domain}"
      }
    }
  }
  manager.flush()
  Napier.d(tag = "CchWebView") { "CookieManager.flush() done (cookies.size=${cookies.size})" }
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

private class JsBridge(
  private val onBridgeMessage: (BridgeMessage) -> Unit,
) {
  private val mainHandler = Handler(Looper.getMainLooper())

  @JavascriptInterface
  fun postMessage(message: String) {
    val parsed = BridgeMessage.parse(message)
    mainHandler.post { onBridgeMessage(parsed) }
  }
}
