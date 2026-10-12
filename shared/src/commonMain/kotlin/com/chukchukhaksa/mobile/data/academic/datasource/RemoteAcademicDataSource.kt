package com.chukchukhaksa.mobile.data.academic.datasource

import com.chukchukhaksa.mobile.common.model.academic.AcademicRecord
import com.chukchukhaksa.mobile.common.model.academic.AcademicSummary

interface RemoteAcademicDataSource {
  suspend fun getAcademicSummary(): AcademicSummary
  suspend fun getAcademicRecord(): AcademicRecord
}
