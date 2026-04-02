package com.chukchukhaksa.mobile.domain.profile.repository

import com.chukchukhaksa.mobile.common.model.profile.Profile

interface ProfileRepository {
  suspend fun getProfileInfo(): Profile
}
