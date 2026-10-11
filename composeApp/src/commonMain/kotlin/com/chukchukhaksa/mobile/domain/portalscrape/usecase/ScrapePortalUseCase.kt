package com.chukchukhaksa.mobile.domain.portalscrape.usecase

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeResult
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.portalscrape.repository.PortalScrapeRepository

class ScrapePortalUseCase(
  private val portalScrapeRepository: PortalScrapeRepository,
) {
  suspend operator fun invoke(
    studentId: String,
    password: String,
    onProgress: (PortalScrapeProgress) -> Unit = {},
  ): Result<PortalScrapeResult> = runCatchingIgnoreCancelled {
    portalScrapeRepository.scrape(studentId, password, onProgress)
  }
}
