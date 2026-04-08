package com.chukchukhaksa.mobile.data.graduation.datasource

import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessListData

interface RemoteGraduationDataSource {
  suspend fun getGraduationProgress(): GraduationProcessListData
}
