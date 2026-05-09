package com.chukchukhaksa.mobile.presentation.webview

import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import io.github.aakira.napier.Napier

sealed interface BridgeAction {
  data class NavigateWebView(val absoluteUrl: String) : BridgeAction
  data class Unhandled(val reason: String, val raw: String) : BridgeAction
}

fun BridgeMessage.toAction(currentHost: String): BridgeAction = when (this) {
  is BridgeMessage.Navigate -> {
    val invalidReason = validatePath(path)
    if (invalidReason != null) {
      Napier.w(tag = "BridgeAction") {
        "InvalidPath: reason=$invalidReason, raw=$path"
      }
      BridgeAction.Unhandled(reason = "InvalidPath", raw = path)
    } else {
      BridgeAction.NavigateWebView(absoluteUrl = "https://$currentHost$path")
    }
  }

  is BridgeMessage.Unknown -> {
    Napier.w(tag = "BridgeAction") {
      "UnsupportedPrefix: raw=$raw"
    }
    BridgeAction.Unhandled(reason = "UnsupportedPrefix", raw = raw)
  }
}

private fun validatePath(path: String): String? {
  val pathOnly = path.substringBefore('?').substringBefore('#')
  if (!pathOnly.startsWith('/')) return "PathMustStartWithSlash"
  if (pathOnly.startsWith("//")) return "DomainNotAllowed"
  if (pathOnly.contains("://")) return "SchemeNotAllowed"
  if (pathOnly.split('/').any { it == ".." }) return "TraversalNotAllowed"
  return null
}
