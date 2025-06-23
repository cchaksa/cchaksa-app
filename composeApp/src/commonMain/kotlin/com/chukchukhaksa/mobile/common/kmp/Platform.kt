package com.chukchukhaksa.mobile.common.kmp

import androidx.compose.runtime.Composable

enum class Platform {
    Android,
    IOS
}

expect fun getPlatform(): Platform

@Composable
expect fun SetStatusBarColor()
