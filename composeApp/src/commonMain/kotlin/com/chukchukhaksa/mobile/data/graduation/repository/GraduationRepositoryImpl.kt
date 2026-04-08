package com.chukchukhaksa.mobile.data.graduation.repository

import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessListData
import com.chukchukhaksa.mobile.data.graduation.datasource.RemoteGraduationDataSource
import com.chukchukhaksa.mobile.domain.graduation.repository.GraduationRepository

class GraduationRepositoryImpl(
  private val remoteGraduationDataSource: RemoteGraduationDataSource,
) : GraduationRepository {
  override suspend fun getGraduationProgress(): GraduationProcessListData {
    return remoteGraduationDataSource.getGraduationProgress()
  }
}
