package com.chukchukhaksa.mobile.domain.auth.usecase

import com.chukchukhaksa.mobile.common.kmp.AppleSignInResult
import com.chukchukhaksa.mobile.common.kmp.AppleSignInClient
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled

class AppleLoginUseCase(
    private val appleSignInClient: AppleSignInClient,
) {
    suspend operator fun invoke(): Result<AppleSignInResult> = runCatchingIgnoreCancelled {
        appleSignInClient.signIn()
    }
}
