package com.chukchukhaksa.mobile.domain.analytics.usecase

import com.chukchukhaksa.mobile.common.analytics.AnalyticsClient
import com.chukchukhaksa.mobile.common.kmp.getAppVersionName
import com.chukchukhaksa.mobile.common.kmp.getPlatform
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetAllTimetableUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetMainTimetableUseCase

/**
 * Amplitude user properties를 세팅한다.
 *
 * - sys_app_version: 앱 버전 (정적)
 * - sys_platform: "android" / "ios" (정적, 네이티브에서만 set)
 * - timetable_count: 전체 시간표 개수 (로컬 DB 스냅샷)
 * - course_count: 메인 시간표의 과목 개수 (로컬 DB 스냅샷)
 *
 * 앱 시작·로그인·포그라운드 복귀 시점에 호출되며, 실패해도 앱 흐름을 막지 않도록 [Result]로 감싼다.
 */
class SetAnalyticsUserPropertiesUseCase(
    private val getAllTimetableUseCase: GetAllTimetableUseCase,
    private val getMainTimetableUseCase: GetMainTimetableUseCase,
    private val analyticsClient: AnalyticsClient,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingIgnoreCancelled {
        val timetableCount = getAllTimetableUseCase().getOrNull()?.size ?: 0
        val courseCount = getMainTimetableUseCase().getOrNull()?.cellList?.size ?: 0
        analyticsClient.setUserProperties(
            mapOf(
                "sys_app_version" to getAppVersionName(),
                "sys_platform" to getPlatform().name.lowercase(),
                "timetable_count" to timetableCount,
                "course_count" to courseCount,
            ),
        )
    }
}
