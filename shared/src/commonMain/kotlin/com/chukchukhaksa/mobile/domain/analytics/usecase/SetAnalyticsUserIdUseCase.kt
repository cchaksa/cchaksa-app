package com.chukchukhaksa.mobile.domain.analytics.usecase

import com.chukchukhaksa.mobile.common.analytics.AnalyticsClient
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.remote.user.UserApi

/**
 * 백엔드에서 Amplitude용 사용자 식별자를 받아 [AnalyticsClient]에 설정한다.
 *
 * 식별 실패가 앱 흐름(앱 시작·로그인)을 막지 않도록 [Result]로 감싼다.
 */
class SetAnalyticsUserIdUseCase(
    private val userApi: UserApi,
    private val analyticsClient: AnalyticsClient,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingIgnoreCancelled {
        analyticsClient.setUserId(userApi.getAnalyticsId())
    }
}
