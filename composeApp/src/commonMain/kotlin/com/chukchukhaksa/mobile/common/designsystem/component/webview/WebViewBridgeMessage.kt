package com.chukchukhaksa.mobile.common.designsystem.component.webview

sealed interface BridgeMessage {
  data class Navigate(val path: String) : BridgeMessage
  data object RedirectToHome : BridgeMessage
  data class Unknown(val raw: String) : BridgeMessage

  companion object {
    private const val NAVIGATE_PREFIX = "navigate:"
    private const val REDIRECT_TO_HOME = "redirectToHome"

    fun parse(raw: String): BridgeMessage = when {
      raw.startsWith(NAVIGATE_PREFIX) -> Navigate(path = raw.removePrefix(NAVIGATE_PREFIX))
      raw == REDIRECT_TO_HOME -> RedirectToHome
      else -> Unknown(raw = raw)
    }
  }
}
