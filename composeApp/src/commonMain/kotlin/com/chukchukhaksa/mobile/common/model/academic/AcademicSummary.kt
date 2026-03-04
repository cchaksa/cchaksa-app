package com.chukchukhaksa.mobile.common.model.academic

data class AcademicSummary(
  val cumulativeGpa: Double,
  val percentile: Double,
  val requiredCredits: Int,
  val totalEarnedCredits: Int
)

