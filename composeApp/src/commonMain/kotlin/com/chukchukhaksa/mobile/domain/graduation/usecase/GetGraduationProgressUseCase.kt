package com.chukchukhaksa.mobile.domain.graduation.usecase

import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessListData
import com.chukchukhaksa.mobile.common.model.response.graduation.GraduationProcessResponseData
import com.chukchukhaksa.mobile.domain.academic.repository.AcademicRepository
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.graduation.repository.GraduationRepository

class GetGraduationProgressUseCase(
  private val graduationRepository: GraduationRepository,
) {
  suspend operator fun invoke(): Result<GraduationProcessListData> =
      runCatchingIgnoreCancelled {
        graduationRepository.getGraduationProgress()
      }
}
