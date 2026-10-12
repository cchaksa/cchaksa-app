package com.chukchukhaksa.mobile

import android.app.Application
import com.chukchukhaksa.mobile.common.kmp.AndroidAppConfig
import com.chukchukhaksa.mobile.di.initKoinAndroid
import com.kakao.sdk.common.KakaoSdk
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.initialize
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class ChukChukHaksaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val appConfig = AndroidAppConfig(
            isDebug = BuildConfig.DEBUG,
            versionName = BuildConfig.VERSION_NAME,
            amplitudeApiKey = if (BuildConfig.DEBUG) BuildConfig.AMPLITUDE_API_KEY_DEV else BuildConfig.AMPLITUDE_API_KEY_PROD,
            admobInterstitialAdUnitId = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID,
        )

        // isDebug는 initKoinAndroid가 설정을 설치한 뒤에야 읽을 수 있으므로 여기서는 appConfig 값을 쓴다.
        if (appConfig.isDebug) {
            Napier.base(DebugAntilog())
        }

        initKoinAndroid(application = this, appConfig = appConfig)

        Firebase.initialize(this)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        MobileAds.initialize(this)
    }
}
