package com.chukchukhaksa.mobile.common.designsystem.component.webview

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun CchWebView(
  url: String,
  controller: CchWebViewController,
  modifier: Modifier,
) {
  val webView = remember {
    object : Any() {
      var instance: WebView? = null
    }
  }

  DisposableEffect(controller) {
    controller.goBackAction = {
      webView.instance?.let { if (it.canGoBack()) it.goBack() }
    }
    onDispose {
      controller.goBackAction = null
    }
  }

  AndroidView(
    modifier = modifier,
    factory = { context ->
      WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webViewClient = object : WebViewClient() {
          override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            controller.canGoBack = view?.canGoBack() == true
          }
        }
        webView.instance = this
        loadUrl(url)
      }
    },
    onRelease = { view ->
      webView.instance = null
      view.destroy()
    },
  )
}
