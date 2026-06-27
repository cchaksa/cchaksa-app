package com.chukchukhaksa.mobile.common.kmp

actual class AdvertisingIdProvider actual constructor() {
    /** iOS 전용(IDFA) 기능이므로 Android에서는 미지원으로 보고한다. */
    actual fun getAdvertisingIdInfo(): AdvertisingIdInfo =
        AdvertisingIdInfo(
            id = null,
            isValid = false,
            diagnostics = "Android 미지원(IDFA는 iOS 전용)",
        )
}
