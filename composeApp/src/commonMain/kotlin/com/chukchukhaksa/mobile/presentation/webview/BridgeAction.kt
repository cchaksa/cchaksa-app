package com.chukchukhaksa.mobile.presentation.webview

import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import io.github.aakira.napier.Napier

sealed interface BridgeAction {
  data class NavigateWebView(val absoluteUrl: String) : BridgeAction

  /**
   * 앱 홈으로 이동한다.
   * @param reloadWebView true이면 홈 웹뷰를 다시 로드한다(예: 포털 연동 완료 후 최신 상태 반영).
   */
  data class RedirectToHome(val reloadWebView: Boolean = false) : BridgeAction

  /**
   * 웹뷰 뒤로가기. 웹뷰 히스토리가 남아 있으면 웹뷰에서 뒤로 가고,
   * 더 이상 뒤로 갈 수 없으면 네이티브 네비게이션을 pop 한다.
   */
  data object NavigateBack : BridgeAction

  /** 회원 탈퇴 처리. 로그인(랜딩) 화면으로 이동한다. */
  data object Withdraw : BridgeAction

  /** 웹 콘텐츠 렌더링 완료. shimmer 로딩 뷰를 제거한다. */
  data object ContentRendered : BridgeAction
  data class Unhandled(val reason: String, val raw: String) : BridgeAction
}

fun BridgeMessage.toAction(currentHost: String): BridgeAction = when (this) {
  is BridgeMessage.RedirectToHome -> BridgeAction.RedirectToHome(reloadWebView = false)

  // 포털 연동 완료 → 홈으로 이동하며 연동 결과가 반영되도록 홈 웹뷰를 재로드한다.
  is BridgeMessage.PortalLinkDone -> BridgeAction.RedirectToHome(reloadWebView = true)

  is BridgeMessage.NavigateBack -> BridgeAction.NavigateBack

  is BridgeMessage.Withdraw -> BridgeAction.Withdraw

  is BridgeMessage.Rendered -> BridgeAction.ContentRendered

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
      // 보이지 않는 문자/길이 차이로 인한 미스매치를 진단하기 위해 따옴표·길이·코드포인트를 함께 남긴다.
      "UnsupportedPrefix: raw='$raw' (len=${raw.length}, codes=${raw.map { it.code }})"
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
