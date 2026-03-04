package com.chukchukhaksa.mobile.domain.auth.usecase

import com.chukchukhaksa.mobile.common.kmp.KakaoSignInClient
import com.chukchukhaksa.mobile.domain.auth.model.SignInResult
import com.chukchukhaksa.mobile.domain.auth.repository.AuthRepository
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled

class KakaoLoginUseCase(
    private val kakaoSignInClient: KakaoSignInClient,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(context: Any? = null): Result<SignInResult> =
        runCatchingIgnoreCancelled {
            val kakaoResult = kakaoSignInClient.signIn(context)
            val signInResult = authRepository.signIn(
                idToken = kakaoResult.idToken,
                nonce = kakaoResult.nonce,
            )
            authRepository.saveTokens(
                accessToken = signInResult.accessToken,
                refreshToken = signInResult.refreshToken,
            )
            signInResult
        }
}
