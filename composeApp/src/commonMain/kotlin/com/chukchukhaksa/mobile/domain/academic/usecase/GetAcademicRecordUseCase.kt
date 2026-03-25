package com.chukchukhaksa.mobile.domain.academic.usecase

import com.chukchukhaksa.mobile.common.model.academic.AcademicRecord
import com.chukchukhaksa.mobile.common.model.academic.AcademicSummary
import com.chukchukhaksa.mobile.domain.academic.repository.AcademicRepository
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled

class GetAcademicRecordUseCase(
  private val academicRepository: AcademicRepository,
) {
  suspend operator fun invoke(): Result<AcademicRecord?> = runCatchingIgnoreCancelled {
    with(academicRepository) {
      getAcademicRecord()
    }
  }
}
