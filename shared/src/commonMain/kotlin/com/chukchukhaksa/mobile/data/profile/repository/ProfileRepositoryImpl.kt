package com.chukchukhaksa.mobile.data.profile.repository

import com.chukchukhaksa.mobile.common.model.profile.Profile
import com.chukchukhaksa.mobile.data.profile.datasource.RemoteProfileDataSource
import com.chukchukhaksa.mobile.domain.profile.repository.ProfileRepository

class ProfileRepositoryImpl(
  private val remoteProfileDataSource: RemoteProfileDataSource,
) : ProfileRepository {
  override suspend fun getProfileInfo(): Profile {
    return remoteProfileDataSource.getProfileData()
  }
}
