package com.chukchukhaksa.mobile.data.auth.repository

import com.chukchukhaksa.mobile.data.auth.datasource.RemoteAuthDataSource
import com.chukchukhaksa.mobile.domain.auth.model.SignInResult
import com.chukchukhaksa.mobile.domain.auth.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteAuthDataSource: RemoteAuthDataSource,
) : AuthRepository {

    override suspend fun signIn(idToken: String, nonce: String): SignInResult {
        return remoteAuthDataSource.signIn(idToken = idToken, nonce = nonce)
    }
}
