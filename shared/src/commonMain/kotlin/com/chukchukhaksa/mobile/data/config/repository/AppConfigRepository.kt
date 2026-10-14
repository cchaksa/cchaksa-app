package com.chukchukhaksa.mobile.data.config.repository

import com.chukchukhaksa.mobile.data.config.model.ForceUpdateInfo

interface AppConfigRepository {
    /** [currentVersion]이 현재 플랫폼의 최소 지원 버전보다 낮으면 강제 업데이트와 스토어 URL을 돌려준다. */
    suspend fun getForceUpdateInfo(currentVersion: String): ForceUpdateInfo
}
