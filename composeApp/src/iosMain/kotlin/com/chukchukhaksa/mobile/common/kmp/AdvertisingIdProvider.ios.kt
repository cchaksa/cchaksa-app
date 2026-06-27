package com.chukchukhaksa.mobile.common.kmp

import platform.AdSupport.ASIdentifierManager
import platform.AppTrackingTransparency.ATTrackingManager
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusAuthorized
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusDenied
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusNotDetermined
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusRestricted

/** ATT 미허용 시 IDFA 자리에 채워지는 0값 UUID. */
private const val ZERO_IDFA = "00000000-0000-0000-0000-000000000000"

actual class AdvertisingIdProvider actual constructor() {
    actual fun getAdvertisingIdInfo(): AdvertisingIdInfo {
        val idfa = ASIdentifierManager.sharedManager().advertisingIdentifier?.UUIDString
        val attStatus = attStatusText()
        val isValid = idfa != null && idfa != ZERO_IDFA

        val diagnostics = buildString {
            append("ATT 권한: ")
            append(attStatus)
            if (!isValid) {
                append("\n실패 원인: ")
                append(
                    when {
                        idfa == null -> "IDFA 미조회(null)"
                        idfa == ZERO_IDFA -> "ATT 미허용으로 0값 IDFA 반환"
                        else -> "알 수 없음"
                    }
                )
            }
        }

        return AdvertisingIdInfo(id = idfa, isValid = isValid, diagnostics = diagnostics)
    }

    private fun attStatusText(): String =
        when (ATTrackingManager.trackingAuthorizationStatus) {
            ATTrackingManagerAuthorizationStatusAuthorized -> "허용(authorized)"
            ATTrackingManagerAuthorizationStatusDenied -> "거부(denied)"
            ATTrackingManagerAuthorizationStatusNotDetermined -> "미결정(notDetermined)"
            ATTrackingManagerAuthorizationStatusRestricted -> "제한(restricted)"
            else -> "알 수 없음"
        }
}
