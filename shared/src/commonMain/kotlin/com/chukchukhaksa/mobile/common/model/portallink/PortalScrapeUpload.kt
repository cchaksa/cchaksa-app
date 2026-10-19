package com.chukchukhaksa.mobile.common.model.portallink

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeResult

/**
 * 서버에 올릴 기기 스크래핑 결과 한 건. 결과가 확정될 때 [idempotencyKey]를 한 번 만들고, 업로드를 다시 시도하는 동안
 * 같은 결과와 같은 키를 함께 보낸다. 서버는 같은 키로 다시 받은 같은 결과를 두 번 반영하지 않는다.
 * 결과에 개인 정보가 들어 있어 toString에 드러나지 않도록 data class로 두지 않는다.
 */
class PortalScrapeUpload(
  val scrapeResult: PortalScrapeResult,
  val idempotencyKey: String,
)
