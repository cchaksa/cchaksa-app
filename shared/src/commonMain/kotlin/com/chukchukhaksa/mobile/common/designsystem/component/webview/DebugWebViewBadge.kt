package com.chukchukhaksa.mobile.common.designsystem.component.webview

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chukchukhaksa.mobile.common.kmp.isDebug

/**
 * 디버그 빌드에서 WebView 화면 우측 상단에 "DEBUG: WEBVIEW" 배지를 표기한다.
 * 릴리즈 빌드에서는 아무것도 그리지 않는다. [BoxScope] 안에서 호출해야 한다.
 */
@Composable
fun BoxScope.DebugWebViewBadge() {
  if (!isDebug) return

  Text(
    text = "DEBUG: WEBVIEW",
    color = Color.Red,
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp,
    modifier = Modifier
      .align(Alignment.TopEnd)
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(top = 4.dp, end = 12.dp),
  )
}
