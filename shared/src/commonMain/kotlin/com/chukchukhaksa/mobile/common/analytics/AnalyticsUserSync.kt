package com.chukchukhaksa.mobile.common.analytics

import com.chukchukhaksa.mobile.common.kmp.getAppVersionName
import com.chukchukhaksa.mobile.common.kmp.getPlatform
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository
import com.chukchukhaksa.mobile.remote.user.UserApi

/**
 * Amplitude 사용자 식별과 user properties를 [AnalyticsClient]에 반영한다.
 *
 * 식별·갱신 실패가 앱 흐름(앱 시작·로그인)을 막지 않도록 [Result]로 감싼다.
 */
class AnalyticsUserSync(
    private val userApi: UserApi,
    private val timetableRepository: TimetableRepository,
    private val analyticsClient: AnalyticsClient,
) {
    /** 백엔드에서 Amplitude용 사용자 식별자를 받아 설정한다. */
    suspend fun identify(): Result<Unit> = runCatchingIgnoreCancelled {
        analyticsClient.setUserId(userApi.getAnalyticsId())
    }

    /**
     * Amplitude user properties를 세팅한다.
     *
     * - sys_app_version: 앱 버전 (정적)
     * - sys_platform: "android" / "ios" (정적, 네이티브에서만 set)
     * - timetable_count: 전체 시간표 개수 (로컬 DB 스냅샷)
     * - course_count: 메인 시간표의 과목 개수 (로컬 DB 스냅샷)
     *
     * 앱 시작·로그인·포그라운드 복귀 시점에 호출된다. 개수를 읽지 못하면 0으로 보낸다.
     */
    suspend fun syncUserProperties(): Result<Unit> = runCatchingIgnoreCancelled {
        val timetables = runCatchingIgnoreCancelled {
            timetableRepository.getAllTimetable()
        }.getOrNull().orEmpty()
        val mainCreateTime = runCatchingIgnoreCancelled {
            timetableRepository.getMainTimetableCreateTime()
        }.getOrNull()
        val timetableCount = timetables.size
        // 메인 시간표는 이미 읽은 목록 안에 있으므로 DB를 다시 조회하지 않는다.
        val courseCount = timetables.firstOrNull { it.createTime == mainCreateTime }?.cellList?.size ?: 0
        analyticsClient.setUserProperties(
            mapOf(
                "sys_app_version" to getAppVersionName(),
                "sys_platform" to getPlatform().name.lowercase(),
                "timetable_count" to timetableCount,
                "course_count" to courseCount,
            ),
        )
    }

    /** 로그아웃·회원탈퇴 시 사용자 식별을 해제한다. */
    fun clear() {
        analyticsClient.setUserId(null)
    }
}
