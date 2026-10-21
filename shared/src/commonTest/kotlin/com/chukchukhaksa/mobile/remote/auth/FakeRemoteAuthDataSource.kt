package com.chukchukhaksa.mobile.remote.auth

import com.chukchukhaksa.mobile.data.auth.datasource.RemoteAuthDataSource
import com.chukchukhaksa.mobile.data.auth.model.RefreshTokenResult
import com.chukchukhaksa.mobile.data.auth.model.SignInResult

class FakeRemoteAuthDataSource(
    private val signInResult: SignInResult,
) : RemoteAuthDataSource {

    override suspend fun signIn(provider: String, idToken: String, nonce: String): SignInResult = signInResult

    override suspend fun refreshToken(refreshToken: String): RefreshTokenResult =
        error("refreshToken is not expected in this test")
}
