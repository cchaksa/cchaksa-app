package com.chukchukhaksa.mobile.data.openlecture.repository

import com.chukchukhaksa.mobile.common.model.OpenLecture

interface OpenLectureRepository {
    suspend fun getOpenLectureList(
        lectureOrProfessorName: String? = null,
        major: String? = null,
        grade: Int? = null,
    ): List<OpenLecture>

    /** 원격 개설강좌 목록이 로컬 캐시보다 새롭거나 캐시가 비어 있으면 전체 목록을 다시 받는다. */
    suspend fun updateAllLecturesIfNeeded()

    suspend fun getLastUpdatedDate(): String?

    suspend fun getOpenMajor(): List<String>
}