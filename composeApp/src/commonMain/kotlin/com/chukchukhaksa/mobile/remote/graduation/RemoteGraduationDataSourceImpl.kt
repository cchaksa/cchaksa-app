package com.chukchukhaksa.mobile.remote.graduation

import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessListData
import com.chukchukhaksa.mobile.data.graduation.datasource.RemoteGraduationDataSource
import com.chukchukhaksa.mobile.presentation.home.graduationprogress.graduationProgressResponseSampleData

class RemoteGraduationDataSourceImpl(): RemoteGraduationDataSource {
  override suspend fun getGraduationProgress(): GraduationProcessListData {
    return graduationProgressResponseSampleData
  }
}
