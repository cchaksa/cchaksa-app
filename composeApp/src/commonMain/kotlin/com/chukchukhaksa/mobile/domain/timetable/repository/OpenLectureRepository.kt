package com.chukchukhaksa.mobile.domain.timetable.repository

import com.chukchukhaksa.mobile.common.model.OpenLecture
import kotlinx.coroutines.flow.Flow

interface OpenLectureRepository {
    suspend fun getOpenLectureList(
        lectureOrProfessorName: String? = null,
        major: String? = null,
        grade: Int? = null,
    ): List<OpenLecture>

    suspend fun checkNeedUpdate(): Boolean

    suspend fun updateAllLectures()

    suspend fun getLastUpdatedDate(): String?

    suspend fun getOpenMajor(): List<String>
}