package com.chukchukhaksa.mobile.data.portalscrape.datasource

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeResult

interface RemotePortalScrapeDataSource {
  suspend fun scrape(
    studentId: String,
    password: String,
    onProgress: (PortalScrapeProgress) -> Unit,
  ): PortalScrapeResult
}
