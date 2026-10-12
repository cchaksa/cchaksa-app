package com.chukchukhaksa.mobile.common.kmp

/**
 * 광고 식별자(IDFA/GAID) 조회를 위한 플랫폼 추상.
 *
 * AdMob 테스트 기기 등록용으로 iOS IDFA를 얻는 데 사용한다(디버그 전용 제스처에서 소비).
 */
expect class AdvertisingIdProvider() {
    /**
     * 디버그 진단용 IDFA 조회 결과를 반환한다.
     *
     * - iOS: IDFA(`ASIdentifierManager.advertisingIdentifier`)와 ATT 권한 상태. ATT 미허용 시 0값 UUID가 나온다.
     * - Android 및 그 외: 미지원(id=null).
     */
    fun getAdvertisingIdInfo(): AdvertisingIdInfo
}

/**
 * IDFA 조회 진단 결과.
 *
 * @param id 조회된 IDFA. iOS ATT 미허용 시 0값 UUID, Android·미지원 시 null.
 * @param isValid 광고 추적에 쓸 수 있는 유효 IDFA인지(null·0값 UUID이면 false).
 * @param diagnostics 실패 원인 추적용으로 사람이 읽는 상태 문자열(ATT 권한 등).
 */
data class AdvertisingIdInfo(
    val id: String?,
    val isValid: Boolean,
    val diagnostics: String,
)
