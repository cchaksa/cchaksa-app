package com.chukchukhaksa.mobile.presentation.webview

import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import io.github.aakira.napier.Napier

sealed interface BridgeAction {
  data class NavigateWebView(val absoluteUrl: String) : BridgeAction

  /**
   * 광고 게이트 경로(AD_GATED_PATHS)로의 이동 요청.
   * 화면이 "광고가 노출됩니다" 다이얼로그 → 전면 광고 → 이동 순서로 오케스트레이션한다.
   */
  data class NavigateWebViewWithAd(val absoluteUrl: String) : BridgeAction

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
      val absoluteUrl = "https://$currentHost$path"
      // 쿼리·프래그먼트를 제외한 경로 부분으로 광고 게이트 여부를 판정한다(validatePath와 동일 기준).
      val pathOnly = path.substringBefore('?').substringBefore('#')
      if (pathOnly in AD_GATED_PATHS) {
        BridgeAction.NavigateWebViewWithAd(absoluteUrl = absoluteUrl)
      } else {
        BridgeAction.NavigateWebView(absoluteUrl = absoluteUrl)
      }
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

/**
 * 전면 광고 게이트를 적용할 경로 집합(쿼리·프래그먼트 제외 기준 비교).
 * 현재 원소는 1개이나 추후 경로 추가 확장을 위해 집합으로 둔다.
 * 노출 경로 변경은 앱 배포가 필요하다(웹이 동적으로 정하지 않음, D2).
 */
private val AD_GATED_PATHS = setOf("/mpa/resync/login")

private fun validatePath(path: String): String? {
  val pathOnly = path.substringBefore('?').substringBefore('#')
  if (!pathOnly.startsWith('/')) return "PathMustStartWithSlash"
  if (pathOnly.startsWith("//")) return "DomainNotAllowed"
  if (pathOnly.contains("://")) return "SchemeNotAllowed"
  if (pathOnly.split('/').any { it == ".." }) return "TraversalNotAllowed"
  return null
}
