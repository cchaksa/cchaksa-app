package com.chukchukhaksa.mobile.common.kmp

import platform.Foundation.NSBundle

actual fun getAppVersionName(): String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        ?: "Unknown"