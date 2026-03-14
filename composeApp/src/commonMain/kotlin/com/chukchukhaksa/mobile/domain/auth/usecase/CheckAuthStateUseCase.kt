package com.chukchukhaksa.mobile.domain.auth.usecase

import com.chukchukhaksa.mobile.domain.auth.repository.AuthRepository
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.remote.common.ApiException
import kotlin.coroutines.cancellation.CancellationException

class CheckAuthStateUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Boolean> = runCatchingIgnoreCancelled {
        val refreshToken = authRepository.getRefreshToken()
            ?: return@runCatchingIgnoreCancelled false

        try {
            authRepository.refreshToken()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            authRepository.clearTokens()
            false
        } catch (e: Exception) {
            false
        }
    }
}
