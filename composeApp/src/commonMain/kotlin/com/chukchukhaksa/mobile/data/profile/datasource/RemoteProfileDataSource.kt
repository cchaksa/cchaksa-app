package com.chukchukhaksa.mobile.data.profile.datasource

import com.chukchukhaksa.mobile.common.model.profile.Profile

interface RemoteProfileDataSource {
  suspend fun getProfileData(): Profile
}
