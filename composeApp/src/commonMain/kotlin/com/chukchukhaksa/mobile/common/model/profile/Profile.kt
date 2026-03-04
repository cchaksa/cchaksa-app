package com.chukchukhaksa.mobile.common.model.profile

data class Profile(
  val name: String,
  val studentCode: String,
  val departmentName: String,
  val majorName: String,
  val gradeLevel: Int,
  val currentSemester: Int,
  val status : String,
)
