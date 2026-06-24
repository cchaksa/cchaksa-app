package com.chukchukhaksa.mobile.common.ad

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * [AdManager]의 iOS 구현(B방식). 실제 GoogleMobileAds 호출은 Swift 구현체 [AdMobBridge]가 담당하고,
 * 본 클래스는 콜백 기반 bridge를 `suspendCancellableCoroutine`로 suspend 결과로 변환한다
 * (`KakaoSignInClient.ios.kt` 동형). 어떤 실패도 예외로 전파하지 않고 [AdShowResult]로 환원한다.
 *
 * 타임아웃은 **로드 단계에만** 10초 적용한다(표시 단계는 사용자가 광고를 닫을 때까지 대기).
 */
class IosAdManager(
    private val bridge: AdMobBridge,
) : AdManager {

    override fun preloadInterstitial(adUnitId: String?) {
        // 선택적 사전 로드: 다이얼로그 표시 시점에 미리 로드해 확인 시 대기를 줄인다(실패는 무시).
        bridge.loadInterstitial(adUnitId, onLoaded = {}, onFailure = {})
    }

    override suspend fun showInterstitial(adUnitId: String?): AdShowResult {
        // 1) 로드(10초 타임아웃). 타임아웃·실패 시 표시 없이 환원한다.
        val loadResult = withTimeoutOrNull(LOAD_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                bridge.loadInterstitial(
                    adUnitId = adUnitId,
                    onLoaded = { if (continuation.isActive) continuation.resume(LoadResult.Loaded) },
                    onFailure = { error ->
                        if (continuation.isActive) continuation.resume(LoadResult.Failed(error.toFailureReason()))
                    },
                )
            }
        }
        when (loadResult) {
            null -> return AdShowResult.Failed(AdFailureReason.Timeout)
            is LoadResult.Failed -> return AdShowResult.Failed(loadResult.reason)
            LoadResult.Loaded -> Unit
        }

        // 2) 표시(타임아웃 없음 — 사용자 시청·닫힘까지 대기).
        return suspendCancellableCoroutine { continuation ->
            bridge.showInterstitial(
                onClosed = { if (continuation.isActive) continuation.resume(AdShowResult.Dismissed) },
                onFailure = {
                    if (continuation.isActive) continuation.resume(AdShowResult.Failed(AdFailureReason.NotReady))
                },
            )
        }
    }

    private fun Throwable.toFailureReason(): AdFailureReason = when (message) {
        REASON_NO_FILL -> AdFailureReason.NoFill
        REASON_NETWORK -> AdFailureReason.Network
        REASON_NOT_READY -> AdFailureReason.NotReady
        else -> AdFailureReason.Network
    }

    private sealed interface LoadResult {
        data object Loaded : LoadResult
        data class Failed(val reason: AdFailureReason) : LoadResult
    }

    private companion object {
        const val LOAD_TIMEOUT_MS = 10_000L

        // Swift 구현체가 onFailure(Throwable)의 message로 전달하는 사유 토큰.
        const val REASON_NO_FILL = "no_fill"
        const val REASON_NETWORK = "network"
        const val REASON_NOT_READY = "not_ready"
    }
}
