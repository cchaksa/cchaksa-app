package com.chukchukhaksa.mobile.data.openmajor.datasource

import com.chukchukhaksa.mobile.common.model.OpenMajor
import kotlinx.coroutines.flow.Flow

interface LocalOpenMajorDataSource {
  suspend fun getLocalOpenMajorVersion(): Flow<Float>
  suspend fun setLocalOpenMajorVersion(version: Float)
  suspend fun getLocalOpenMajorList(): List<OpenMajor>
  suspend fun saveAllOpenMajors(majors: List<OpenMajor>)
  suspend fun deleteAllOpenMajors()
}
