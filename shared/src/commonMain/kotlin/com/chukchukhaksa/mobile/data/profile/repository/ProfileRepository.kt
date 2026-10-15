package com.chukchukhaksa.mobile.data.profile.repository

import com.chukchukhaksa.mobile.common.model.profile.Profile

interface ProfileRepository {
  suspend fun getProfileInfo(): Profile
}
