package com.chukchukhaksa.mobile.common.designsystem.component.webview

import io.github.aakira.napier.Napier
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.darwin.NSObject

internal const val HISTORY_OBSERVER_HANDLER_NAME = "cchHistoryObserver"

/**
 * SPA의 history API(pushState/replaceState/popstate) 이동은 WKNavigationDelegate 콜백을
 * 발생시키지 않아 canGoBack 갱신이 누락된다. 페이지에 주입해 history 변경을 네이티브로 통지한다.
 */
private val HISTORY_OBSERVER_SCRIPT = """
  (function() {
    if (window.__cchHistoryObserverInstalled) { return; }
    window.__cchHistoryObserverInstalled = true;
    function notify() {
      try { window.webkit.messageHandlers.$HISTORY_OBSERVER_HANDLER_NAME.postMessage(''); } catch (e) {}
    }
    var originalPushState = history.pushState;
    history.pushState = function() {
      var result = originalPushState.apply(this, arguments);
      notify();
      return result;
    };
    var originalReplaceState = history.replaceState;
    history.replaceState = function() {
      var result = originalReplaceState.apply(this, arguments);
      notify();
      return result;
    };
    window.addEventListener('popstate', notify);
  })();
""".trimIndent()

internal fun createHistoryObserverScript(): WKUserScript = WKUserScript(
  source = HISTORY_OBSERVER_SCRIPT,
  injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
  forMainFrameOnly = true,
)

/** history 변경 통지를 받아 해당 웹뷰의 canGoBack을 다시 읽도록 콜백한다. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class HistoryScriptMessageHandler(
  private val onHistoryChanged: (WKWebView) -> Unit,
) : NSObject(), WKScriptMessageHandlerProtocol {
  override fun userContentController(
    userContentController: WKUserContentController,
    didReceiveScriptMessage: WKScriptMessage,
  ) {
    val webView = didReceiveScriptMessage.webView ?: return
    Napier.d(tag = "CchWebView") {
      "history changed → canGoBack=${webView.canGoBack}, url=${webView.URL?.absoluteString}"
    }
    onHistoryChanged(webView)
  }
}
