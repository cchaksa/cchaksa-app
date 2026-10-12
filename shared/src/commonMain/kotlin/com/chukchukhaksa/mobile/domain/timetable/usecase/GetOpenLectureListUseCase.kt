package com.chukchukhaksa.mobile.domain.timetable.usecase

import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.timetable.repository.OpenLectureRepository

class GetOpenLectureListUseCase(
    private val openLectureRepository: OpenLectureRepository,
) {
    suspend operator fun invoke(param: Param) = runCatchingIgnoreCancelled {
        with(param) {
            openLectureRepository
                .getOpenLectureList(
                    lectureOrProfessorName = lectureOrProfessorName,
                    major = major,
                    grade = grade,
                )
        }
    }


    data class Param(
        val lectureOrProfessorName: String?,
        val major: String?,
        val grade: Int?,
    )
}