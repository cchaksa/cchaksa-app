package com.chukchukhaksa.mobile.common.designsystem.component.webview

expect class WebViewHolder {
  fun preload(url: String, cookies: List<WebViewCookie> = emptyList())
  fun updateCookies(cookies: List<WebViewCookie>)
  fun isInitialLoaded(): Boolean

  /**
   * 불러온 홈 페이지가 rendered 브릿지 이벤트를 보냈는지. 화면이 붙기 전에 온 이벤트도 기록하며,
   * [reset]이나 새로 불러올 때만 지운다.
   */
  fun isContentRendered(): Boolean
  fun reset()
}
