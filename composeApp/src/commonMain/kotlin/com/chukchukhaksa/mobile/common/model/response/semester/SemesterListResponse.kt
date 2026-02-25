package com.chukchukhaksa.mobile.common.model.response.semester

data class SemesterListResponse(
    val data: List<SemesterListResponseData>,
    val message: String,
    val success: Boolean
)

data class SemesterListResponseData(
  val semester: Int,
  val year: Int
)
