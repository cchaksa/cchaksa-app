package com.chukchukhaksa.mobile.remote.user

import com.chukchukhaksa.mobile.remote.common.ApiResponse
import com.chukchukhaksa.mobile.remote.common.getDataOrThrow
import com.chukchukhaksa.mobile.remote.user.dto.UserResponseData
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class UserApiImpl(
  private val client: HttpClient,
) : UserApi {

  override suspend fun getMe(): UserInfo {
    Napier.d(tag = "UserApi") { "GET $USER_ME_PATH" }
    val data = client.get(USER_ME_PATH)
      .body<ApiResponse<UserResponseData>>()
      .getDataOrThrow()
    Napier.d(tag = "UserApi") { "getMe success (isPortalLinked=${data.isPortalLinked})" }
    return UserInfo(isPortalLinked = data.isPortalLinked)
  }

  private companion object {
    const val USER_ME_PATH = "users/me"
  }
}
