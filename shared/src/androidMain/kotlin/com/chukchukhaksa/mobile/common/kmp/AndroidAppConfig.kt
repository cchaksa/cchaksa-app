package com.chukchukhaksa.mobile.common.kmp

import android.os.Build

/**
 * 앱 모듈이 자기 BuildConfig로 만들어 넘기는 Android 빌드 설정.
 * 공유 모듈은 단일 variant라 debug/release 값을 스스로 알 수 없다.
 */
data class AndroidAppConfig(
    val isDebug: Boolean,
    val versionName: String,
    val amplitudeApiKey: String,
    val admobInterstitialAdUnitId: String,
) {
    // 로그나 크래시 보고에 객체가 찍혀도 키가 평문으로 남지 않도록 가린다.
    override fun toString(): String =
        "AndroidAppConfig(isDebug=$isDebug, versionName=$versionName, amplitudeApiKey=<redacted>, admobInterstitialAdUnitId=<redacted>)"
}

@Volatile
private var installedConfig: AndroidAppConfig? = null

// Compose 프리뷰(layoutlib)는 Application.onCreate를 거치지 않는다. 설치 없이도 렌더링되도록 디버그 빌드 값으로 대신한다.
private val layoutlibPreviewConfig = AndroidAppConfig(
    isDebug = true,
    versionName = "preview",
    amplitudeApiKey = "",
    admobInterstitialAdUnitId = "",
)

internal fun installAndroidAppConfig(config: AndroidAppConfig) {
    installedConfig = config
}

internal val androidAppConfig: AndroidAppConfig
    get() = installedConfig
        ?: layoutlibPreviewConfig.takeIf { Build.DEVICE == "layoutlib" }
        ?: error("AndroidAppConfig가 설치되지 않았습니다. Application.onCreate에서 initKoinAndroid()를 먼저 호출해야 합니다.")
