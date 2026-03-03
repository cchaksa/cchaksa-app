package com.chukchukhaksa.mobile.common.model.response.semester

import com.chukchukhaksa.mobile.common.model.semester.SemesterGrades
import com.chukchukhaksa.mobile.common.model.semester.SemesterGradesList

data class SemesterGradesListResponse(
    val data: List<SemesterGradesData>,
    val message: String,
    val success: Boolean
) {
  fun toSemesterGradesList() = SemesterGradesList(
    semesterGradesList = data.map { it.toSemesterGrades() }
  )
}

data class SemesterGradesData(
  val attemptedCredits: Int,
  val classRank: Int,
  val earnedCredits: Int,
  val percentile: Double,
  val semester: Int,
  val semesterGpa: Double,
  val totalStudents: Int,
  val year: Int
) {
  fun toSemesterGrades() = SemesterGrades(
    attemptedCredits = attemptedCredits,
    classRank = classRank,
    earnedCredits = earnedCredits,
    percentile = percentile,
    semester = semester,
    semesterGpa = semesterGpa,
    totalStudents = totalStudents,
    year = year,
  )
}
