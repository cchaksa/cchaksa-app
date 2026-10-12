package com.chukchukhaksa.mobile.common.designsystem.component.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class CchWebViewController {
  var canGoBack: Boolean by mutableStateOf(false)
    internal set

  // 페이지 로딩 여부. 첫 로드부터 shimmer가 보이도록 true로 시작하고,
  // onPageStarted/onPageFinished(또는 iOS 내비게이션 델리게이트)에서 갱신한다.
  var isLoading: Boolean by mutableStateOf(true)
    internal set

  internal var goBackAction: (() -> Unit)? = null

  fun goBack() {
    goBackAction?.invoke()
  }
}

@Composable
fun rememberCchWebViewController(): CchWebViewController = remember { CchWebViewController() }

@Composable
expect fun CchWebView(
  url: String,
  controller: CchWebViewController,
  modifier: Modifier = Modifier,
  cookies: List<WebViewCookie> = emptyList(),
  onBridgeMessage: (BridgeMessage) -> Unit = {},
)
