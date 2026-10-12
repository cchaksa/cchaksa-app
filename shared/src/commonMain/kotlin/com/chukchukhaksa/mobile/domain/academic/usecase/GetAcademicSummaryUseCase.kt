package com.chukchukhaksa.mobile.domain.academic.usecase

import com.chukchukhaksa.mobile.common.model.academic.AcademicSummary
import com.chukchukhaksa.mobile.domain.academic.repository.AcademicRepository
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled

class GetAcademicSummaryUseCase(
  private val academicRepository: AcademicRepository,
) {
  suspend operator fun invoke(): Result<AcademicSummary?> = runCatchingIgnoreCancelled {
    with(academicRepository) {
      getAcademicSummary()
    }
  }
}
