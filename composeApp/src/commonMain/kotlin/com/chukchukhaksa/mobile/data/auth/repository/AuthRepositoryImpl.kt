package com.chukchukhaksa.mobile.data.auth.repository

import com.chukchukhaksa.mobile.data.auth.datasource.LocalAuthDataSource
import com.chukchukhaksa.mobile.data.auth.datasource.RemoteAuthDataSource
import com.chukchukhaksa.mobile.domain.auth.model.SignInResult
import com.chukchukhaksa.mobile.domain.auth.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val localAuthDataSource: LocalAuthDataSource,
) : AuthRepository {

    override suspend fun signIn(idToken: String, nonce: String): SignInResult {
        return remoteAuthDataSource.signIn(idToken = idToken, nonce = nonce)
    }

    override suspend fun refreshToken() {
        val token = localAuthDataSource.getRefreshToken()
            ?: throw IllegalStateException("No refresh token")
        val result = remoteAuthDataSource.refreshToken(token)
        saveTokens(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
        )
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        localAuthDataSource.saveAccessToken(accessToken)
        localAuthDataSource.saveRefreshToken(refreshToken)
    }

    override suspend fun getAccessToken(): String? {
        return localAuthDataSource.getAccessToken()
    }

    override suspend fun getRefreshToken(): String? {
        return localAuthDataSource.getRefreshToken()
    }

    override suspend fun clearTokens() {
        localAuthDataSource.clearTokens()
    }
}
