package com.chukchukhaksa.mobile.common.designsystem.component.webview

import kotlin.test.Test
import kotlin.test.assertEquals

class WebViewUserAgentTest {

  @Test
  fun `Android debug UA`() {
    assertEquals(
      "Android/3.0.1/debug",
      formatWebViewUserAgent(platform = "Android", version = "3.0.1", debug = true),
    )
  }

  @Test
  fun `iOS debug UA`() {
    assertEquals(
      "iOS/3.0.1/debug",
      formatWebViewUserAgent(platform = "iOS", version = "3.0.1", debug = true),
    )
  }

  @Test
  fun `iOS prod UA`() {
    assertEquals(
      "iOS/3.0.1/prod",
      formatWebViewUserAgent(platform = "iOS", version = "3.0.1", debug = false),
    )
  }

  @Test
  fun `Android prod UA`() {
    assertEquals(
      "Android/3.0.1/prod",
      formatWebViewUserAgent(platform = "Android", version = "3.0.1", debug = false),
    )
  }

  @Test
  fun `Unknown version is passed through`() {
    assertEquals(
      "iOS/Unknown/prod",
      formatWebViewUserAgent(platform = "iOS", version = "Unknown", debug = false),
    )
  }
}
