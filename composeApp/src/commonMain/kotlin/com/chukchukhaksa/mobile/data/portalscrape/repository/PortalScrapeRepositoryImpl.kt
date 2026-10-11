package com.chukchukhaksa.mobile.data.portalscrape.repository

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeResult
import com.chukchukhaksa.mobile.data.portalscrape.datasource.RemotePortalScrapeDataSource
import com.chukchukhaksa.mobile.domain.portalscrape.repository.PortalScrapeRepository

class PortalScrapeRepositoryImpl(
  private val remotePortalScrapeDataSource: RemotePortalScrapeDataSource,
) : PortalScrapeRepository {
  override suspend fun scrape(
    studentId: String,
    password: String,
    onProgress: (PortalScrapeProgress) -> Unit,
  ): PortalScrapeResult = remotePortalScrapeDataSource.scrape(studentId, password, onProgress)
}
