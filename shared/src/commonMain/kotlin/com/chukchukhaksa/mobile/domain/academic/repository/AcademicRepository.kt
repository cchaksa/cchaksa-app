package com.chukchukhaksa.mobile.domain.academic.repository

import com.chukchukhaksa.mobile.common.model.academic.AcademicRecord
import com.chukchukhaksa.mobile.common.model.academic.AcademicSummary

interface AcademicRepository {
  suspend fun getAcademicSummary(): AcademicSummary
  suspend fun getAcademicRecord(): AcademicRecord
}
