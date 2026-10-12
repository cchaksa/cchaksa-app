package com.chukchukhaksa.mobile.data.academic.repository

import com.chukchukhaksa.mobile.common.model.academic.AcademicRecord
import com.chukchukhaksa.mobile.common.model.academic.AcademicSummary
import com.chukchukhaksa.mobile.data.academic.datasource.RemoteAcademicDataSource
import com.chukchukhaksa.mobile.domain.academic.repository.AcademicRepository

class AcademicRepositoryImpl(
  private val remoteAcademicDataSource: RemoteAcademicDataSource,
) : AcademicRepository {

  override suspend fun getAcademicSummary(): AcademicSummary {
    return remoteAcademicDataSource.getAcademicSummary()
  }

  override suspend fun getAcademicRecord(): AcademicRecord {
    return remoteAcademicDataSource.getAcademicRecord()
  }
}
