package com.chukchukhaksa.mobile.data.portallink.model

/** 서버 스크래핑 job의 상태(`GET /portal/link/jobs/{jobId}`). */
data class PortalLinkJob(
  val status: PortalLinkJobStatus,
  val errorCode: String?,
)

enum class PortalLinkJobStatus {
  IN_PROGRESS,
  SUCCEEDED,
  FAILED,
}
