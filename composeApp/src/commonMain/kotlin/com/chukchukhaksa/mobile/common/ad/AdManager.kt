package com.chukchukhaksa.mobile.common.ad

/**
 * 전면(Interstitial) 광고를 표시하는 공통 추상.
 *
 * `commonMain`에는 계약만 두고, 실제 AdMob SDK 호출은 플랫폼 구현체
 * (Android `AndroidAdManager` / iOS `IosAdManager`)가 담당해 `platformModule`로 주입한다.
 * 웹뷰 navigate 게이트 오케스트레이션은 이 추상에만 의존한다(역방향 채널·보상형 미도입).
 */
interface AdManager {
  /**
   * 전면 광고를 로드한 뒤 표시한다(사전 로드 캐시 없음 — 호출 시점에 로드한다).
   * 로드·표시 실패는 예외로 전파하지 않고 [AdShowResult.Failed]로 환원한다.
   *
   * @param adUnitId 사용할 광고단위 ID. null이면 구현체가 빌드 설정(BuildConfig / Info.plist)의 기본 ID로 폴백한다.
   */
  suspend fun showInterstitial(adUnitId: String? = null): AdShowResult
}

/** 전면 광고 표시 결과. */
sealed interface AdShowResult {
  /** 광고가 정상적으로 표시된 뒤 닫혔다. */
  data object Dismissed : AdShowResult

  /** 로드·표시에 실패했다(no-fill·네트워크·타임아웃·즉시표시불가). */
  data class Failed(val reason: AdFailureReason) : AdShowResult
}

/** 전면 광고 실패 사유. */
enum class AdFailureReason {
  /** 노출 가능한 광고 인벤토리가 없음(no-fill). */
  NoFill,

  /** 네트워크 오류. */
  Network,

  /** 로드 타임아웃. */
  Timeout,

  /** 표시 시점에 준비된 광고가 없음. */
  NotReady,
}
