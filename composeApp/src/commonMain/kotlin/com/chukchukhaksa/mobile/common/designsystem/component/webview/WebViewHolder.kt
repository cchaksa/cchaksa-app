package com.chukchukhaksa.mobile.common.designsystem.component.webview

expect class WebViewHolder {
  fun preload(url: String, cookies: List<WebViewCookie> = emptyList())
  fun updateCookies(cookies: List<WebViewCookie>)
  fun isInitialLoaded(): Boolean
  fun reset()
}
