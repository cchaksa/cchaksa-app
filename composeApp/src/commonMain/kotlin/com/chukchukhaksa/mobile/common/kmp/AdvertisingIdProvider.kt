package com.chukchukhaksa.mobile.common.kmp

/**
 * 광고 식별자(IDFA/GAID) 조회를 위한 플랫폼 추상.
 *
 * AdMob 테스트 기기 등록용으로 iOS IDFA를 얻는 데 사용한다(디버그 전용 제스처에서 소비).
 */
expect class AdvertisingIdProvider() {
    /**
     * 광고 식별자를 반환한다.
     *
     * - iOS: IDFA(`ASIdentifierManager.advertisingIdentifier`). ATT 미허용 시 0값 UUID가 나온다.
     * - Android 및 그 외: null(미지원).
     */
    fun getAdvertisingId(): String?
}
