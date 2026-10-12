package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository

class GetMainTimetableUseCase(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke(): Result<Timetable?> = runCatchingIgnoreCancelled {
        with(timetableRepository) {
            val createTime = getMainTimetableCreateTime() ?: return@with null
            getTimetable(createTime)
        }
    }
}