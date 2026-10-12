package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class InsertTimetableUseCase(
    private val timetableRepository: TimetableRepository,
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(param: Param): Result<Unit> = runCatchingIgnoreCancelled {
        val createTime = Clock.System.now().toEpochMilliseconds()
        with(timetableRepository) {
            insertTimetable(
                Timetable(
                    createTime = createTime,
                    year = param.year,
                    semester = param.semester,
                    name = param.name,
                    cellList = emptyList(),
                ),
            )
            setMainTimetableCreateTime(createTime)
        }
    }

    data class Param(
        val name: String,
        val year: String,
        val semester: String,
    )
}