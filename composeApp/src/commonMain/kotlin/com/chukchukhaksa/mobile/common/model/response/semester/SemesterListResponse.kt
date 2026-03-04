package com.chukchukhaksa.mobile.common.model.response.semester

import com.chukchukhaksa.mobile.common.model.semester.SemesterList
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.Semester

data class SemesterListResponse(
    val data: List<SemesterData>,
    val message: String,
    val success: Boolean
) {
  fun toSemesterList() = SemesterList(
    semesterList = data.map { it.toSemester() }
  )
}

data class SemesterData(
  val semester: Int,
  val year: Int
) {
  fun toSemester() = Semester(
    semester = semester.toString(),
    year = year.toString(),
  )
}
