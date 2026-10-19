package com.chukchukhaksa.mobile.data.portallink.datasource

import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJob
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJobStatus

/** 척척학사 서버의 포털 연동 API. */
interface RemotePortalLinkDataSource {
  /**
   * 기기에서 스크래핑한 원문 JSON을 서버에 올린다. 서버는 서버 스크래퍼의 결과와 같은 방식으로 저장을 마친 뒤 응답한다.
   * 같은 [idempotencyKey]로 같은 결과를 다시 보내면 서버는 다시 반영하지 않고 처음 결과를 돌려준다.
   * 계약은 .docs/requirements/portal-scrape-upload-api.md에 정리했다.
   *
   * @throws com.chukchukhaksa.mobile.remote.common.ApiException 서버가 요청을 거부했다. 같은 요청을 다시 보내도 결과가 같다.
   * 그 밖의 예외(5xx, 네트워크 오류, 응답 해석 실패)는 저장됐는지 알 수 없는 일시 실패다.
   */
  suspend fun uploadScrapeResult(rawJson: String, idempotencyKey: String)

  /** 서버 스크래핑 job을 만들고 job id를 돌려준다. */
  suspend fun createLinkJob(studentId: String, password: String, idempotencyKey: String): String

  suspend fun getLinkJob(jobId: String): PortalLinkJob

  /** job 요약의 상태. 폴링이 제한 시간을 넘긴 뒤 실제로 성공했는지 한 번 더 확인할 때 쓴다. */
  suspend fun getLinkJobSummaryStatus(jobId: String): PortalLinkJobStatus
}
