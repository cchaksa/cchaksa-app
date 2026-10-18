package com.chukchukhaksa.mobile.data.portalscrape.repository

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeResult

interface PortalScrapeRepository {
  /** 수원대 포털에 로그인해 학생·수강·성적·지정과목을 조회한다. 계정 정보는 저장하지 않는다. */
  suspend fun scrape(
    studentId: String,
    password: String,
    onProgress: (PortalScrapeProgress) -> Unit,
  ): PortalScrapeResult
}
