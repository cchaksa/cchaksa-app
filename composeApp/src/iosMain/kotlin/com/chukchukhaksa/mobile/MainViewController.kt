package com.chukchukhaksa.mobile

import androidx.compose.ui.window.ComposeUIViewController
import com.chukchukhaksa.mobile.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }