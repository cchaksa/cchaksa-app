package com.chukchukhaksa.mobile.domain.analytics.usecase

import com.chukchukhaksa.mobile.common.analytics.AnalyticsClient

/**
 * 로그아웃·회원탈퇴 시 Amplitude 사용자 식별을 해제한다.
 */
class ClearAnalyticsUserIdUseCase(
    private val analyticsClient: AnalyticsClient,
) {
    operator fun invoke() {
        analyticsClient.setUserId(null)
    }
}
