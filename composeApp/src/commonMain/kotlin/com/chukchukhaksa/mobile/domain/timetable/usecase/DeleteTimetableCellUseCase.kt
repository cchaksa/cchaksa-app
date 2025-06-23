package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository

class DeleteTimetableCellUseCase(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke(cell: TimetableCell): Result<Timetable> = runCatchingIgnoreCancelled {
        timetableRepository.deleteTimetableCell(cell = cell)
    }
}
