package com.chukchukhaksa.mobile.local.datasource.openlecture.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.chukchukhaksa.mobile.common.model.OpenLecture
import com.chukchukhaksa.mobile.data.openlecture.datasource.LocalOpenLectureDataSource
import com.chukchukhaksa.mobile.local.database.openlecture.database.OpenLectureDatabase
import com.chukchukhaksa.mobile.local.datasource.openlecture.converter.toEntity
import com.chukchukhaksa.mobile.local.datasource.openlecture.converter.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalOpenLectureDatasourceImpl(
  private val dataStore: DataStore<Preferences>,
  private val openLectureDatabase: OpenLectureDatabase,
) : LocalOpenLectureDataSource {
  private val ioDispatcher = Dispatchers.IO

  companion object {
    private val OPEN_LECTURE_LIST_VERSION = longPreferencesKey("[KEY] is open lecture list version")
  }

  private val data: Flow<Preferences>
    get() = dataStore.data

  override fun getOpenLectureListVersion(): Flow<Long?> {
    return data.map { it[OPEN_LECTURE_LIST_VERSION] }
  }

  override suspend fun setOpenLectureListVersion(version: Long) {
    dataStore.edit { it[OPEN_LECTURE_LIST_VERSION] = version }
  }

  override suspend fun getOpenLectureList(
    lectureOrProfessorName: String?,
    major: String?,
    grade: Int?,
  ): List<OpenLecture> {
    return openLectureDatabase
      .openLectureDao()
      .searchLectures(
        lectureOrProfessorName = lectureOrProfessorName,
        major = major,
        grade = grade,
      ).map { list -> list.toModel() }
  }

  override suspend fun updateAllLectures(lectures: List<OpenLecture>) = withContext(ioDispatcher) {
    openLectureDatabase
      .openLectureDao()
      .updateAllLectures(lectures.map { it.toEntity() })
  }
}
