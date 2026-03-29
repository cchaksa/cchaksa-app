package com.chukchukhaksa.mobile.remote.auth

import com.chukchukhaksa.mobile.data.auth.datasource.LocalAuthDataSource

class FakeLocalAuthDataSource(
    initialAccessToken: String? = null,
    initialRefreshToken: String? = null,
) : LocalAuthDataSource {
    private var accessToken: String? = initialAccessToken
    private var refreshToken: String? = initialRefreshToken

    override suspend fun saveAccessToken(token: String) {
        accessToken = token
    }

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun saveRefreshToken(token: String) {
        refreshToken = token
    }

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}
