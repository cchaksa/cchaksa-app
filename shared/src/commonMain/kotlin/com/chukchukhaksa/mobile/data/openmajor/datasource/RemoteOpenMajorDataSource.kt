package com.chukchukhaksa.mobile.data.openmajor.datasource

interface RemoteOpenMajorDataSource {
  suspend fun getOpenMajorVersion(): Float
  suspend fun getOpenMajorList(): List<String>
}
