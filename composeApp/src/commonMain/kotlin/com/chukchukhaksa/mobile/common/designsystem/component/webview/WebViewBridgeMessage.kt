package com.chukchukhaksa.mobile.common.designsystem.component.webview

sealed interface BridgeMessage {
  data class Navigate(val path: String) : BridgeMessage
  data class Unknown(val raw: String) : BridgeMessage

  companion object {
    private const val NAVIGATE_PREFIX = "navigate:"

    fun parse(raw: String): BridgeMessage = if (raw.startsWith(NAVIGATE_PREFIX)) {
      Navigate(path = raw.removePrefix(NAVIGATE_PREFIX))
    } else {
      Unknown(raw = raw)
    }
  }
}
