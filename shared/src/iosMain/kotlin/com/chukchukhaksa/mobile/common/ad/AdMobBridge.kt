package com.chukchukhaksa.mobile.common.ad

/**
 * iOS 전면 광고 B방식 계약. 실제 `GoogleMobileAds` 호출은 Swift 구현체 `AdMobBridgeImpl`이 담당하고,
 * `MainViewController`가 Koin에 주입한다(`KakaoLoginBridge` 선례 동형).
 *
 * 로드와 표시를 분리해, [IosAdManager]가 **로드에만 10초 타임아웃**을 적용하고
 * 표시(시청)는 사용자가 닫을 때까지 대기하도록 한다(Android `AndroidAdManager`와 동일한 타임아웃 경계).
 */
interface AdMobBridge {
    /**
     * 전면 광고를 로드한다. 이미 로드된 광고가 있으면 구현체가 즉시 [onLoaded]를 호출할 수 있다.
     *
     * @param adUnitId 사용할 광고단위 ID. null이면 구현체가 Info.plist의 기본 ID로 폴백한다.
     * @param onLoaded 로드 성공 시 호출.
     * @param onFailure 로드 실패 시 호출(no-fill·네트워크 등).
     */
    fun loadInterstitial(
        adUnitId: String?,
        onLoaded: () -> Unit,
        onFailure: (Throwable) -> Unit,
    )

    /**
     * 직전에 로드된 전면 광고를 표시한다.
     *
     * @param onClosed 광고가 전체 화면으로 표시된 뒤 닫혔을 때 호출.
     * @param onFailure 표시 실패 시 호출(준비된 광고 없음·표시 컨텍스트 없음 등).
     */
    fun showInterstitial(
        onClosed: () -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}
