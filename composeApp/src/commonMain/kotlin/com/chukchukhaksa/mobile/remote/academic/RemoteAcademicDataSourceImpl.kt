package com.chukchukhaksa.mobile.remote.academic

import com.chukchukhaksa.mobile.common.model.academic.AcademicRecord
import com.chukchukhaksa.mobile.common.model.academic.AcademicSummary
import com.chukchukhaksa.mobile.data.academic.datasource.RemoteAcademicDataSource

class RemoteAcademicDataSourceImpl(): RemoteAcademicDataSource {

  override suspend fun getAcademicSummary(): AcademicSummary {
    return AcademicSummary(
      cumulativeGpa = 4.03,
      percentile = 93.2,
      requiredCredits = 130,
      totalEarnedCredits = 109
    )
  }

  override suspend fun getAcademicRecord(): AcademicRecord {
    TODO("Not yet implemented")
  }
}
