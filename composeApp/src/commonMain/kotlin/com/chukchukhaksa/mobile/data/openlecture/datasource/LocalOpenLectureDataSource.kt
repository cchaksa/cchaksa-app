package com.chukchukhaksa.mobile.data.openlecture.datasource

import com.chukchukhaksa.mobile.common.model.OpenLecture
import kotlinx.coroutines.flow.Flow

interface LocalOpenLectureDataSource {
  fun getOpenLectureListVersion(): Flow<Long?>

  suspend fun setOpenLectureListVersion(version: Long)

  suspend fun getOpenLectureList(
    lectureOrProfessorName: String? = null,
    major: String? = null,
    grade: Int? = null
  ): List<OpenLecture>

  suspend fun updateAllLectures(lectures: List<OpenLecture>)
}
