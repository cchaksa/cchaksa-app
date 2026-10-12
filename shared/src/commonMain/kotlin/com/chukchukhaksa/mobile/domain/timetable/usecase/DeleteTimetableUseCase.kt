package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository


class DeleteTimetableUseCase(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke(timetable: Timetable): Result<Unit> = runCatchingIgnoreCancelled {
        with(timetableRepository) {
            deleteTimetable(timetable)

            val mainTimetableCreateTime = getMainTimetableCreateTime()
            if (mainTimetableCreateTime != timetable.createTime) return@runCatchingIgnoreCancelled

            val firstTimetableCreateTime = getAllTimetable().firstOrNull()?.createTime ?: 0L
            setMainTimetableCreateTime(firstTimetableCreateTime)
        }
    }
}