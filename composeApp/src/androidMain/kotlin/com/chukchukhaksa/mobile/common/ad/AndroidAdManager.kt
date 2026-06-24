package com.chukchukhaksa.mobile.common.ad

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.chukchukhaksa.mobile.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

/**
 * [AdManager]의 Android 구현. Google Mobile Ads SDK의 [InterstitialAd]로 전면 광고를
 * 로드·표시하고, 닫힘/실패를 [AdShowResult]로 환원한다. 어떤 실패도 예외로 전파하지 않는다.
 *
 * 표시(`show`)에는 포그라운드 [Activity]가 필요하지만 본 객체는 Application Context로 생성되므로,
 * 생성자에서 [Application.ActivityLifecycleCallbacks]를 등록해 현재 Activity를 [WeakReference]로
 * 추적한다(기존 `AndroidAppLifecycleObserver`의 lifecycle-callback 등록 패턴 동형).
 *
 * 로드·표시는 Google Mobile Ads SDK 요구사항에 따라 메인 스레드에서 수행한다.
 */
class AndroidAdManager(application: Application) : AdManager {

    private val appContext: Context = application.applicationContext
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var activityRef: WeakReference<Activity>? = null
    private var preloadedAd: InterstitialAd? = null

    init {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    activityRef = WeakReference(activity)
                }

                override fun onActivityPaused(activity: Activity) {
                    if (activityRef?.get() === activity) activityRef = null
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            },
        )
    }

    override fun preloadInterstitial(adUnitId: String?) {
        val unitId = adUnitId ?: BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID
        if (unitId.isBlank() || preloadedAd != null) return
        scope.launch {
            when (val outcome = loadInterstitial(unitId)) {
                is LoadOutcome.Loaded -> preloadedAd = outcome.ad
                is LoadOutcome.Failed -> Napier.d(tag = TAG) { "전면 광고 preload 실패: ${outcome.reason}" }
            }
        }
    }

    override suspend fun showInterstitial(adUnitId: String?): AdShowResult = withContext(Dispatchers.Main) {
        val unitId = adUnitId ?: BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID
        if (unitId.isBlank()) return@withContext AdShowResult.Failed(AdFailureReason.NotReady)

        // 프리로드분이 있으면 즉시 소비, 없으면 즉시 로드(10초 타임아웃) 시도.
        val ad = preloadedAd?.also { preloadedAd = null }
            ?: when (val outcome = loadInterstitial(unitId)) {
                is LoadOutcome.Loaded -> outcome.ad
                is LoadOutcome.Failed -> return@withContext AdShowResult.Failed(outcome.reason)
            }

        val activity = activityRef?.get()
            ?: return@withContext AdShowResult.Failed(AdFailureReason.NotReady)

        showLoadedAd(ad, activity)
    }

    /** [InterstitialAd] 로드를 10초 타임아웃과 함께 suspend 결과로 변환한다. 타임아웃 시 [AdFailureReason.Timeout]. */
    private suspend fun loadInterstitial(adUnitId: String): LoadOutcome {
        val outcome = withTimeoutOrNull(LOAD_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                InterstitialAd.load(
                    appContext,
                    adUnitId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            if (continuation.isActive) continuation.resume(LoadOutcome.Loaded(ad))
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            if (continuation.isActive) {
                                continuation.resume(LoadOutcome.Failed(error.toFailureReason()))
                            }
                        }
                    },
                )
            }
        }
        return outcome ?: LoadOutcome.Failed(AdFailureReason.Timeout)
    }

    /** 로드된 광고를 표시하고 닫힘/표시 실패를 [AdShowResult]로 변환한다. */
    private suspend fun showLoadedAd(ad: InterstitialAd, activity: Activity): AdShowResult =
        suspendCancellableCoroutine { continuation ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    if (continuation.isActive) continuation.resume(AdShowResult.Dismissed)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    if (continuation.isActive) continuation.resume(AdShowResult.Failed(AdFailureReason.NotReady))
                }
            }
            ad.show(activity)
        }

    private fun LoadAdError.toFailureReason(): AdFailureReason = when (code) {
        AdRequest.ERROR_CODE_NO_FILL -> AdFailureReason.NoFill
        AdRequest.ERROR_CODE_NETWORK_ERROR -> AdFailureReason.Network
        else -> AdFailureReason.Network
    }

    private sealed interface LoadOutcome {
        data class Loaded(val ad: InterstitialAd) : LoadOutcome
        data class Failed(val reason: AdFailureReason) : LoadOutcome
    }

    private companion object {
        const val TAG = "AndroidAdManager"
        const val LOAD_TIMEOUT_MS = 10_000L
    }
}
