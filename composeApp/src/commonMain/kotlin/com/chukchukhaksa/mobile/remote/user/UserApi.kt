package com.chukchukhaksa.mobile.remote.user

interface UserApi {
  suspend fun getMe(): UserInfo
}

data class UserInfo(
  val isPortalLinked: Boolean,
)
