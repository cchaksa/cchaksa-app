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
  fun `parses redirectToHome ignoring surrounding whitespace and invisible chars`() {
    // 선두 BOM(U+FEFF) + 공백 + 후행 zero-width space(U+200B) + 개행을 둘러싼 케이스
    val raw = "${0xFEFF.toChar()}  redirectToHome${0x200B.toChar()}\n"
    assertEquals(
      BridgeMessage.RedirectToHome,
      BridgeMessage.parse(raw),
    )
  }

  @Test
  fun `parses done portal-link`() {
    assertEquals(
      BridgeMessage.PortalLinkDone,
      BridgeMessage.parse("done:portal-link"),
    )
  }

  @Test
  fun `parses navigateBack`() {
    assertEquals(
      BridgeMessage.NavigateBack,
      BridgeMessage.parse("navigateBack"),
    )
  }

  @Test
  fun `parses withdraw`() {
    assertEquals(
      BridgeMessage.Withdraw,
      BridgeMessage.parse("withdraw"),
    )
  }

  @Test
  fun `parses rendered`() {
    assertEquals(
      BridgeMessage.Rendered,
      BridgeMessage.parse("rendered"),
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
