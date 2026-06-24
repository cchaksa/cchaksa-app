package com.chukchukhaksa.mobile.presentation.webview

import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BridgeActionTest {

  private val host = "dv.cchaksa.com"

  @Test
  fun `simple navigate path becomes absolute url`() {
    val action = BridgeMessage.Navigate(path = "/mpa/some-page").toAction(host)
    assertEquals(
      BridgeAction.NavigateWebView("https://dv.cchaksa.com/mpa/some-page"),
      action,
    )
  }

  @Test
  fun `ad gated path becomes navigate web view with ad`() {
    val action = BridgeMessage.Navigate(path = "/mpa/graduation-progress").toAction(host)
    assertEquals(
      BridgeAction.NavigateWebViewWithAd("https://dv.cchaksa.com/mpa/graduation-progress"),
      action,
    )
  }

  @Test
  fun `ad gated path with query and fragment still gates and preserves url`() {
    val action = BridgeMessage.Navigate(path = "/mpa/graduation-progress?from=home#top").toAction(host)
    assertEquals(
      BridgeAction.NavigateWebViewWithAd("https://dv.cchaksa.com/mpa/graduation-progress?from=home#top"),
      action,
    )
  }

  @Test
  fun `non gated path becomes plain navigate web view`() {
    val action = BridgeMessage.Navigate(path = "/timetable").toAction(host)
    assertEquals(
      BridgeAction.NavigateWebView("https://dv.cchaksa.com/timetable"),
      action,
    )
  }

  @Test
  fun `path with query and fragment is preserved`() {
    val action = BridgeMessage.Navigate(path = "/mpa/foo?x=1#bar").toAction(host)
    assertEquals(
      BridgeAction.NavigateWebView("https://dv.cchaksa.com/mpa/foo?x=1#bar"),
      action,
    )
  }

  @Test
  fun `path that does not start with slash is unhandled`() {
    val action = BridgeMessage.Navigate(path = "mpa/no-slash").toAction(host)
    assertIs<BridgeAction.Unhandled>(action)
    assertEquals("InvalidPath", action.reason)
  }

  @Test
  fun `protocol relative path is unhandled`() {
    val action = BridgeMessage.Navigate(path = "//malicious.com/mpa/x").toAction(host)
    assertIs<BridgeAction.Unhandled>(action)
    assertEquals("InvalidPath", action.reason)
  }

  @Test
  fun `path containing scheme is unhandled`() {
    val action = BridgeMessage.Navigate(path = "/redirect/https://evil.com").toAction(host)
    assertIs<BridgeAction.Unhandled>(action)
    assertEquals("InvalidPath", action.reason)
  }

  @Test
  fun `traversal path is unhandled`() {
    val action = BridgeMessage.Navigate(path = "/mpa/../admin").toAction(host)
    assertIs<BridgeAction.Unhandled>(action)
    assertEquals("InvalidPath", action.reason)
  }

  @Test
  fun `redirectToHome message becomes redirect to home action without reload`() {
    val action = BridgeMessage.RedirectToHome.toAction(host)
    assertEquals(BridgeAction.RedirectToHome(reloadWebView = false), action)
  }

  @Test
  fun `portalLinkDone message becomes redirect to home action with reload`() {
    val action = BridgeMessage.PortalLinkDone.toAction(host)
    assertEquals(BridgeAction.RedirectToHome(reloadWebView = true), action)
  }

  @Test
  fun `navigateBack message becomes navigate back action`() {
    val action = BridgeMessage.NavigateBack.toAction(host)
    assertEquals(BridgeAction.NavigateBack, action)
  }

  @Test
  fun `withdraw message becomes withdraw action`() {
    val action = BridgeMessage.Withdraw.toAction(host)
    assertEquals(BridgeAction.Withdraw, action)
  }

  @Test
  fun `rendered message becomes content rendered action`() {
    val action = BridgeMessage.Rendered.toAction(host)
    assertEquals(BridgeAction.ContentRendered, action)
  }

  @Test
  fun `unknown message becomes unsupported prefix`() {
    val action = BridgeMessage.Unknown(raw = "hello").toAction(host)
    assertIs<BridgeAction.Unhandled>(action)
    assertEquals("UnsupportedPrefix", action.reason)
    assertEquals("hello", action.raw)
  }
}
