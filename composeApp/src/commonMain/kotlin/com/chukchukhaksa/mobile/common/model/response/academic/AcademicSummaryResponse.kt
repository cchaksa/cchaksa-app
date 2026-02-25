package com.chukchukhaksa.mobile.common.model.response.academic

data class AcademicSummaryResponse(
    val data: AcademicSummaryResponseData,
    val message: String,
    val success: Boolean
)

data class AcademicSummaryResponseData(
  val cumulativeGpa: Double,
  val percentile: Double,
  val requiredCredits: Int,
  val totalEarnedCredits: Int
)

