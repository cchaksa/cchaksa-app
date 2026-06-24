package com.chukchukhaksa.mobile.common.kmp

import platform.AdSupport.ASIdentifierManager

actual class AdvertisingIdProvider actual constructor() {
    /**
     * IDFA를 반환한다. ATT 권한이 허용되지 않은 경우 `00000000-0000-0000-0000-000000000000`이 나온다.
     */
    actual fun getAdvertisingId(): String? =
        ASIdentifierManager.sharedManager().advertisingIdentifier?.UUIDString
}
