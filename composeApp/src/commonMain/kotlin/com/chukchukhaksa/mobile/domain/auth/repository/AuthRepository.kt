package com.chukchukhaksa.mobile.domain.auth.repository

import com.chukchukhaksa.mobile.domain.auth.model.SignInResult

interface AuthRepository {
    suspend fun signIn(idToken: String, nonce: String): SignInResult
}
