package com.chukchukhaksa.mobile.remote.auth

import com.chukchukhaksa.mobile.data.auth.datasource.RemoteAuthDataSource
import com.chukchukhaksa.mobile.domain.auth.model.RefreshTokenResult
import com.chukchukhaksa.mobile.domain.auth.model.SignInResult
import com.chukchukhaksa.mobile.remote.auth.model.RefreshRequest
import com.chukchukhaksa.mobile.remote.auth.model.RefreshResponse
import com.chukchukhaksa.mobile.remote.auth.model.SignInRequest
import com.chukchukhaksa.mobile.remote.auth.model.SignInResponse
import com.chukchukhaksa.mobile.remote.common.ApiResponse
import com.chukchukhaksa.mobile.remote.common.getDataOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class RemoteAuthDataSourceImpl(
    private val httpClient: HttpClient,
) : RemoteAuthDataSource {

    override suspend fun signIn(idToken: String, nonce: String): SignInResult {
        val response = httpClient.post("users/signin") {
            setBody(SignInRequest(idToken = idToken, nonce = nonce))
        }.body<ApiResponse<SignInResponse>>()

        return response.getDataOrThrow().toSignInResult()
    }

    override suspend fun refreshToken(refreshToken: String): RefreshTokenResult {
        val response = httpClient.post("api/auth/refresh") {
            setBody(RefreshRequest(refreshToken = refreshToken))
        }.body<ApiResponse<RefreshResponse>>()

        return response.getDataOrThrow().toRefreshTokenResult()
    }
}

private fun SignInResponse.toSignInResult() = SignInResult(
    accessToken = accessToken,
    refreshToken = refreshToken,
    isPortalLinked = isPortalLinked,
)

private fun RefreshResponse.toRefreshTokenResult() = RefreshTokenResult(
    accessToken = accessToken,
    refreshToken = refreshToken,
)
