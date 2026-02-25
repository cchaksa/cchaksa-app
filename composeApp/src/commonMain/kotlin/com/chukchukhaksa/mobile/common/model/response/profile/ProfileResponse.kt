package com.chukchukhaksa.mobile.common.model.response.profile

data class ProfileResponse(
  val success: Boolean,
  val data: Profile,
  val message: String,
)

data class Profile(
  val name: String,
  val studentCode: String,
  val departmentName: String,
  val majorName: String,
  val gradeLevel: Int,
  val currentSemester: Int,
  val status : String,
  val lastUpdatedAt: String,
  val lastSyncedAt : String,
  val reconnectionRequired: Boolean
)
