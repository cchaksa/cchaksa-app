package com.chukchukhaksa.mobile.data.portallink.repository

import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkException
import com.chukchukhaksa.mobile.common.model.portallink.PortalScrapeUpload
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkStrategy

interface PortalLinkRepository {
  /**
   * 기기에서 포털을 스크래핑해 서버에 올린다. 기기 쪽 문제로 보이는 스크래핑 실패와 일부 조회가 실패한 결과에서만
   * 서버 스크래핑 job으로 넘어간다. 디버그 빌드에서는 디버그 화면에서 고른 경로([PortalLinkStrategy])를 따른다.
   * 계정 정보는 저장하지 않는다.
   *
   * @throws PortalLinkException
   */
  suspend fun link(studentId: String, password: String)

  /**
   * 저장됐는지 확인하지 못한 업로드를 같은 결과·같은 키로 다시 올린다.
   *
   * @throws PortalLinkException
   */
  suspend fun upload(upload: PortalScrapeUpload)
}
