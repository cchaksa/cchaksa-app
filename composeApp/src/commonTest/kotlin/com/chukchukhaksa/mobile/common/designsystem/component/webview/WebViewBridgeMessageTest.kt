package com.chukchukhaksa.mobile.common.designsystem.component.webview

import kotlin.test.Test
import kotlin.test.assertEquals

class WebViewBridgeMessageTest {

  @Test
  fun `parses navigate prefix`() {
    assertEquals(
      BridgeMessage.Navigate(path = "/foo"),
      BridgeMessage.parse("navigate:/foo"),
    )
  }

  @Test
  fun `parses redirectToHome`() {
    assertEquals(
      BridgeMessage.RedirectToHome,
      BridgeMessage.parse("redirectToHome"),
    )
  }

  @Test
  fun `treats other strings as unknown`() {
    assertEquals(
      BridgeMessage.Unknown(raw = "hello"),
      BridgeMessage.parse("hello"),
    )
  }

  @Test
  fun `empty string is unknown`() {
    assertEquals(
      BridgeMessage.Unknown(raw = ""),
      BridgeMessage.parse(""),
    )
  }

  @Test
  fun `navigate with empty path returns navigate with empty string`() {
    assertEquals(
      BridgeMessage.Navigate(path = ""),
      BridgeMessage.parse("navigate:"),
    )
  }
}
