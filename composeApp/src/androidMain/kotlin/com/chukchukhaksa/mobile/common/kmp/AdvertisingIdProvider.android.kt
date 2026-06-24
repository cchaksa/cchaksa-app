package com.chukchukhaksa.mobile.common.kmp

actual class AdvertisingIdProvider actual constructor() {
    /** iOS 전용(IDFA) 기능이므로 Android에서는 미지원으로 null을 반환한다. */
    actual fun getAdvertisingId(): String? = null
}
