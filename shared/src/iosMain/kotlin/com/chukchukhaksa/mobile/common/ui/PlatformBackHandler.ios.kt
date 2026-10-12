package com.chukchukhaksa.mobile.common.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
  // iOS: edge swipe is handled by WKWebView when canGoBack=true,
  // and by Compose's own recognizer when canGoBack=false.
  // There is no system back button on iOS, so this is intentionally empty.
}
