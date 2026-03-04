package com.chukchukhaksa.mobile.common.model.response.profile

import com.chukchukhaksa.mobile.common.model.profile.Profile

data class ProfileResponse(
  val success: Boolean,
  val data: ProfileResponseData,
  val message: String,
)

data class ProfileResponseData(
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
) {
  fun toProfile() = Profile(
    name = name,
    studentCode = studentCode,
    departmentName = departmentName,
    majorName = majorName,
    gradeLevel = gradeLevel,
    currentSemester = currentSemester,
    status = status
  )
}
