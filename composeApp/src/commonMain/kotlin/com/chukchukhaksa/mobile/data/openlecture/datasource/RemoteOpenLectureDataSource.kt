package com.chukchukhaksa.mobile.data.openlecture.datasource

import com.chukchukhaksa.mobile.data.openlecture.OpenLectureRaw

interface RemoteOpenLectureDataSource {
  suspend fun getOpenLectureListVersion(): Long
  suspend fun getOpenLectureList(): List<OpenLectureRaw>
}
