package com.chukchukhaksa.mobile.data.auth.datasource

interface LocalAuthDataSource {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
}
