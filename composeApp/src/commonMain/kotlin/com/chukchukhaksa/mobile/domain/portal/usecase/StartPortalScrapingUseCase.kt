package com.chukchukhaksa.mobile.domain.portal.usecase

import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.portal.model.ScrapingResult
import com.chukchukhaksa.mobile.domain.portal.repository.PortalRepository

class StartPortalScrapingUseCase(
    private val portalRepository: PortalRepository,
) {
    suspend operator fun invoke(): Result<ScrapingResult> = runCatchingIgnoreCancelled {
        portalRepository.startScraping()
    }
}
