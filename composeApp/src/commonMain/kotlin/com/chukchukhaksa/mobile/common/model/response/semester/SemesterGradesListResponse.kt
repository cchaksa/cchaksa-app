package com.chukchukhaksa.mobile.common.model.response.semester

data class SemesterGradesListResponse(
    val data: List<SemesterGradesListResponseData>,
    val message: String,
    val success: Boolean
)

data class SemesterGradesListResponseData(
  val attemptedCredits: Int,
  val classRank: Int,
  val earnedCredits: Int,
  val percentile: Double,
  val semester: Int,
  val semesterGpa: Double,
  val totalStudents: Int,
  val year: Int
)
