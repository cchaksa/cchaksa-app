package com.chukchukhaksa.mobile.common.model.graduation

data class GraduationProcessListData(
  val graduationProgress: List<GraduationProcess> = listOf(),
  val hasDifferentGraduationRequirement: Boolean = false
)

data class GraduationProcess(
  val areaType: String,
  val completedElectiveCourses: Int,
  val courses: List<GraduationProcessCourse>,
  val earnedCredits: Int,
  val requiredCredits: Int,
  val requiredElectiveCourses: Int,
  val totalElectiveCourses: Int
)

data class GraduationProcessCourse(
  val courseName: String,
  val credits: Int,
  val grade: String,
  val semester: Int,
  val year: Int
)
