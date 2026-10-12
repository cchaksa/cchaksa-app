package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository

class GetTimetableCellTypeUseCase(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke() = runCatchingIgnoreCancelled {
        timetableRepository.getTimetableCellType()
    }
}