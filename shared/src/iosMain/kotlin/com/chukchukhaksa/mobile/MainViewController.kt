package com.chukchukhaksa.mobile

import androidx.compose.ui.window.ComposeUIViewController
import com.chukchukhaksa.mobile.common.ad.AdMobBridge
import com.chukchukhaksa.mobile.common.analytics.AmplitudeBridge
import com.chukchukhaksa.mobile.common.kmp.KakaoLoginBridge
import com.chukchukhaksa.mobile.di.initKoin

fun MainViewController(
    kakaoLoginBridge: KakaoLoginBridge,
    amplitudeBridge: AmplitudeBridge,
    adMobBridge: AdMobBridge,
) = ComposeUIViewController(
    configure = {
        initKoin {
            modules(
                org.koin.dsl.module {
                    factory<KakaoLoginBridge> { kakaoLoginBridge }
                    factory<AmplitudeBridge> { amplitudeBridge }
                    factory<AdMobBridge> { adMobBridge }
                }
            )
        }
    }
) { App() }
