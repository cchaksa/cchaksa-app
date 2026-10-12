package com.chukchukhaksa.mobile.common.model.semester

data class SemesterGradesList(
    val semesterGradesList: List<SemesterGrades>,
)

data class SemesterGrades(
  val attemptedCredits: Int,
  val classRank: Int,
  val earnedCredits: Int,
  val percentile: Double,
  val semester: Int,
  val semesterGpa: Double,
  val totalStudents: Int,
  val year: Int
)
