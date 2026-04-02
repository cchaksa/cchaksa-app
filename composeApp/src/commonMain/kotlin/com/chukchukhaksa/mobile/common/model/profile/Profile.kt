package com.chukchukhaksa.mobile.common.model.profile

data class Profile(
  val name: String = "",
  val studentCode: String = "",
  val departmentName: String = "",
  val majorName: String = "",
  val dualMajorName: String = "",
  val gradeLevel: Int = 0,
  val currentSemester: Int = 0,
  val status : String = "",
)
