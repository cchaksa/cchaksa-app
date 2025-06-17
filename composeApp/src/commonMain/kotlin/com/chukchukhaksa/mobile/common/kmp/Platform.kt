package com.chukchukhaksa.mobile.common.kmp

enum class Platform {
    Android,
    IOS
}

expect fun getPlatform(): Platform