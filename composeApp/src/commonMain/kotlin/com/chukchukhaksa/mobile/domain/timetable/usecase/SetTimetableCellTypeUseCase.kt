package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository

class SetTimetableCellTypeUseCase(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke(type: String) = runCatchingIgnoreCancelled {
        timetableRepository.setTimetableCellType(type)
    }
}