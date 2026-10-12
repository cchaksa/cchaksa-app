package com.chukchukhaksa.mobile.domain.auth.usecase

import com.chukchukhaksa.mobile.common.kmp.AppleSignInClient
import com.chukchukhaksa.mobile.domain.auth.model.SignInResult
import com.chukchukhaksa.mobile.domain.auth.repository.AuthRepository
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled

class AppleLoginUseCase(
    private val appleSignInClient: AppleSignInClient,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<SignInResult> = runCatchingIgnoreCancelled {
        val appleResult = appleSignInClient.signIn()
        val signInResult = authRepository.signIn(
            provider = "APPLE",
            idToken = appleResult.identityToken,
            nonce = appleResult.nonce,
        )
        authRepository.saveTokens(
            accessToken = signInResult.accessToken,
            refreshToken = signInResult.refreshToken,
        )
        signInResult
    }
}
