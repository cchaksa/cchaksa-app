package com.chukchukhaksa.mobile.remote.user

interface UserApi {
  suspend fun getMe(): UserInfo
  suspend fun getAnalyticsId(): String
}

data class UserInfo(
  val isPortalLinked: Boolean,
)
