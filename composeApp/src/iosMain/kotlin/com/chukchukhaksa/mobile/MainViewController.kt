package com.chukchukhaksa.mobile

import androidx.compose.ui.window.ComposeUIViewController
import com.chukchukhaksa.mobile.common.kmp.KakaoLoginBridge
import com.chukchukhaksa.mobile.di.initKoin

fun MainViewController(
    kakaoLoginBridge: KakaoLoginBridge,
) = ComposeUIViewController(
    configure = {
        initKoin {
            modules(
                org.koin.dsl.module {
                    factory<KakaoLoginBridge> { kakaoLoginBridge }
                }
            )
        }
    }
) { App() }
