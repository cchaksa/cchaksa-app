package com.chukchukhaksa.mobile.common.designsystem.component.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CchHomeWebView(
  holder: WebViewHolder,
  controller: CchWebViewController,
  modifier: Modifier = Modifier,
  cookies: List<WebViewCookie> = emptyList(),
  onBridgeMessage: (BridgeMessage) -> Unit = {},
)
