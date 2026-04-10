package com.chukchukhaksa.mobile.domain.portal.usecase

import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.portal.repository.PortalRepository

class PortalLoginUseCase(
    private val portalRepository: PortalRepository,
) {
    suspend operator fun invoke(
        username: String,
        password: String,
    ): Result<Unit> = runCatchingIgnoreCancelled {
        portalRepository.login(username, password)
    }
}
