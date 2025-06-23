package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository

class InsertTimetableCellUseCase(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke(cellList: List<TimetableCell>): Result<Unit> =
        runCatchingIgnoreCancelled {
            timetableRepository.insertTimetableCell(
                cellList = cellList,
            )
        }
}