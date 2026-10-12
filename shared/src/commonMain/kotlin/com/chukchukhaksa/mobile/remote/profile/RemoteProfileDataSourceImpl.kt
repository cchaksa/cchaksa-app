package com.chukchukhaksa.mobile.remote.profile

import com.chukchukhaksa.mobile.common.model.profile.Profile
import com.chukchukhaksa.mobile.data.profile.datasource.RemoteProfileDataSource

class RemoteProfileDataSourceImpl(): RemoteProfileDataSource {
  override suspend fun getProfileData(): Profile {
    return Profile(
      name = "김척척",
      studentCode = "18234032",
      departmentName = "정보통신학부",
      majorName = "정보통신공학과",
      gradeLevel = 18,
      currentSemester = 6,
      status = "재학중",
      dualMajorName = "컴퓨터공학과"
    )
  }
}
