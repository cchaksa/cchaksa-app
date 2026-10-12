package com.chukchukhaksa.mobile.common.model.academic

data class AcademicSummary(
  val cumulativeGpa: Double = 0.0,
  val percentile: Double = 0.0,
  val requiredCredits: Int = 0,
  val totalEarnedCredits: Int = 0
)

