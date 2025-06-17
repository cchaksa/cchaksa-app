package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.TimetableRepository

class SetMainTimetableCreateTime(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke(createTime: Long): Result<Unit> = runCatchingIgnoreCancelled {
        timetableRepository.setMainTimetableCreateTime(createTime)
    }
}