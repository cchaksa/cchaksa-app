package com.chukchukhaksa.mobile.domain.auth.repository

import com.chukchukhaksa.mobile.domain.auth.model.SignInResult

interface AuthRepository {
    suspend fun signIn(idToken: String, nonce: String): SignInResult
    suspend fun refreshToken()
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
}
