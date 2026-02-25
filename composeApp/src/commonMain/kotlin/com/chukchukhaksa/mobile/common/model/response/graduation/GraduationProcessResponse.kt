package com.chukchukhaksa.mobile.common.model.response.graduation

data class GraduationProcessResponse(
    val data: GraduationProcessResponseData,
    val message: String,
    val success: Boolean
)

data class GraduationProcessResponseData(
  val graduationProgress: List<GraduationProgress>,
  val hasDifferentGraduationRequirement: Boolean
)

data class GraduationProgress(
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
