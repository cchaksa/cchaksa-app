package com.chukchukhaksa.mobile.common.designsystem.component.webview

sealed interface BridgeMessage {
  data class Navigate(val path: String) : BridgeMessage
  data object RedirectToHome : BridgeMessage
  data object PortalLinkDone : BridgeMessage
  data object NavigateBack : BridgeMessage
  data object Withdraw : BridgeMessage

  /** 웹 콘텐츠가 실제로 화면에 그려졌음을 알리는 이벤트. 네이티브 shimmer 제거 시점으로 사용한다. */
  data object Rendered : BridgeMessage
  data class Unknown(val raw: String) : BridgeMessage

  companion object {
    private const val NAVIGATE_PREFIX = "navigate:"
    private const val REDIRECT_TO_HOME = "redirectToHome"
    private const val PORTAL_LINK_DONE = "done:portal-link"
    private const val NAVIGATE_BACK = "navigateBack"
    private const val WITHDRAW = "withdraw"
    private const val RENDERED = "rendered"

    // 비가시 문자 코드포인트: BOM(U+FEFF), zero-width space(U+200B)
    private const val BOM_CODE = 0xFEFF
    private const val ZERO_WIDTH_SPACE_CODE = 0x200B

    fun parse(raw: String): BridgeMessage {
      // 웹에서 전달된 문자열에 앞뒤 공백·개행·BOM·zero-width 문자가 섞여
      // 정확히 일치하지 않는 경우를 방지하기 위해 비교 전에 정규화한다.
      val message = raw.trim {
        it.isWhitespace() || it.code == BOM_CODE || it.code == ZERO_WIDTH_SPACE_CODE
      }
      return when {
        message.startsWith(NAVIGATE_PREFIX) -> Navigate(path = message.removePrefix(NAVIGATE_PREFIX))
        message == REDIRECT_TO_HOME -> RedirectToHome
        message == PORTAL_LINK_DONE -> PortalLinkDone
        message == NAVIGATE_BACK -> NavigateBack
        message == WITHDRAW -> Withdraw
        message == RENDERED -> Rendered
        else -> Unknown(raw = raw)
      }
    }
  }
}
