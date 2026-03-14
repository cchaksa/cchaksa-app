package com.chukchukhaksa.mobile.data.auth.datasource

import com.chukchukhaksa.mobile.domain.auth.model.RefreshTokenResult
import com.chukchukhaksa.mobile.domain.auth.model.SignInResult

interface RemoteAuthDataSource {
    suspend fun signIn(idToken: String, nonce: String): SignInResult
    suspend fun refreshToken(refreshToken: String): RefreshTokenResult
}
