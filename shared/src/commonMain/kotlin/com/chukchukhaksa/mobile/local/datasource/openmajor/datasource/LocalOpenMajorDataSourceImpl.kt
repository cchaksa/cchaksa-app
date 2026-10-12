package com.chukchukhaksa.mobile.local.datasource.openmajor.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.chukchukhaksa.mobile.common.model.OpenMajor
import com.chukchukhaksa.mobile.data.openmajor.datasource.LocalOpenMajorDataSource
import com.chukchukhaksa.mobile.local.database.openmajor.database.OpenMajorDatabase
import com.chukchukhaksa.mobile.local.datasource.openmajor.converter.toEntity
import com.chukchukhaksa.mobile.local.datasource.openmajor.converter.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalOpenMajorDataSourceImpl(
  private val dataStore: DataStore<Preferences>,
  private val db: OpenMajorDatabase,
) : LocalOpenMajorDataSource {

  private val ioDispatcher = Dispatchers.IO

  companion object {
    private val OPEN_MAJOR_VERSION = floatPreferencesKey("[KEY] is open major version")
  }

  private val data: Flow<Preferences>
    get() = dataStore.data

  override suspend fun getLocalOpenMajorVersion(): Flow<Float> =
    data.map { it[OPEN_MAJOR_VERSION] ?: 0f }

  override suspend fun setLocalOpenMajorVersion(version: Float) {
    dataStore.edit { it[OPEN_MAJOR_VERSION] = version }
  }

  override suspend fun getLocalOpenMajorList(): List<OpenMajor> = withContext(ioDispatcher) {
    db.openMajorDao().getAll().map { it.toModel() }
  }

  override suspend fun saveAllOpenMajors(majors: List<OpenMajor>) = withContext(ioDispatcher) {
    db.openMajorDao().insertAll(majors.map { it.toEntity() })
  }

  override suspend fun deleteAllOpenMajors() = withContext(ioDispatcher) {
    db.openMajorDao().deleteAll()
  }
}
