package com.chukchukhaksa.mobile.common.designsystem.component.webview

import com.chukchukhaksa.mobile.common.kmp.getAppVersionName
import com.chukchukhaksa.mobile.common.kmp.isDebug
import io.github.aakira.napier.Napier

internal fun buildWebViewUserAgent(platform: String): String {
  val version = getAppVersionName()
  if (version == "Unknown") {
    Napier.w(tag = "WebViewUserAgent") { "Unknown app version - UA will contain 'Unknown'" }
  }
  val buildType = if (isDebug) "debug" else "prod"
  return formatWebViewUserAgent(platform = platform, version = version, debug = isDebug)
}

internal fun formatWebViewUserAgent(platform: String, version: String, debug: Boolean): String {
  val buildType = if (debug) "debug" else "prod"
  return "$platform/$version/$buildType"
}
