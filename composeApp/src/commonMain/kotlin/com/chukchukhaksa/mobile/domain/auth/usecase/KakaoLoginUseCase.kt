package com.chukchukhaksa.mobile.domain.auth.usecase

import com.chukchukhaksa.mobile.common.kmp.KakaoSignInClient
import com.chukchukhaksa.mobile.common.kmp.KakaoSignInResult
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled

class KakaoLoginUseCase(
    private val kakaoSignInClient: KakaoSignInClient,
) {
    suspend operator fun invoke(context: Any? = null): Result<KakaoSignInResult> =
        runCatchingIgnoreCancelled {
            kakaoSignInClient.signIn(context)
        }
}
