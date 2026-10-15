package com.chukchukhaksa.mobile.data.profile.repository

import com.chukchukhaksa.mobile.common.model.profile.Profile
import com.chukchukhaksa.mobile.data.profile.datasource.RemoteProfileDataSource

class ProfileRepositoryImpl(
  private val remoteProfileDataSource: RemoteProfileDataSource,
) : ProfileRepository {
  override suspend fun getProfileInfo(): Profile {
    return remoteProfileDataSource.getProfileData()
  }
}
