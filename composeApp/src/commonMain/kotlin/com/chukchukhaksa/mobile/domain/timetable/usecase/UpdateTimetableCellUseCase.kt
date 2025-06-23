package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository

class UpdateTimetableCellUseCase(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke(param: Param): Result<Unit> = runCatchingIgnoreCancelled {
        timetableRepository.updateTimetableCell(
            oldCellId = param.oldCellId,
            cellList = param.cellList
        )
    }

    data class Param(
        val oldCellId: String,
        val cellList: List<TimetableCell>,
    )
}