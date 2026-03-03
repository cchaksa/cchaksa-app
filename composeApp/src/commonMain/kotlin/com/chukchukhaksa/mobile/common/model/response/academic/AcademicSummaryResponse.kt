package com.chukchukhaksa.mobile.common.model.response.academic

import com.chukchukhaksa.mobile.common.model.academic.AcademicSummary

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
) {
  fun toAcademicSummary() = AcademicSummary(
    cumulativeGpa = cumulativeGpa,
    percentile = percentile,
    requiredCredits = requiredCredits,
    totalEarnedCredits = totalEarnedCredits,
  )
}

