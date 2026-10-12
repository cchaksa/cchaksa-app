package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.OpenLectureRepository

class UpdateOpenLectureIfNeedUseCase(
    private val openLectureRepository: OpenLectureRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingIgnoreCancelled {
        if (openLectureRepository.checkNeedUpdate().not()) return@runCatchingIgnoreCancelled
        openLectureRepository.updateAllLectures()
    }
}