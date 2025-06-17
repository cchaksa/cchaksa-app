package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository

class GetAllTimetableUseCase(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke(): Result<List<Timetable>> = runCatchingIgnoreCancelled {
        timetableRepository.getAllTimetable()
    }
}