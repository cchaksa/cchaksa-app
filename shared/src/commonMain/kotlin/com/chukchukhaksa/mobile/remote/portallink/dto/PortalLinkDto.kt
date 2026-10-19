package com.chukchukhaksa.mobile.remote.portallink.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** `POST /portal/link` 요청. */
@Serializable
data class PortalLinkRequest(
  @SerialName("portal_type") val portalType: String,
  val username: String,
  val password: String,
)

/** `POST /portal/link` 응답(202). */
@Serializable
data class PortalLinkAcceptedResponseData(
  @SerialName("job_id") val jobId: String,
)

/** `GET /portal/link/jobs/{jobId}` 응답. */
@Serializable
data class PortalLinkJobResponseData(
  val status: String? = null,
  @SerialName("error_code") val errorCode: String? = null,
)

/** `GET /portal/link/jobs/{jobId}/summary` 응답. 학생 요약 정보는 쓰지 않아 상태만 읽는다. */
@Serializable
data class PortalLinkJobSummaryResponseData(
  val status: String? = null,
)

/**
 * `POST /portal/link/scrape-results` 요청. [payload]는 서버 스크래퍼가 S3에 저장하던 원문과 구조가 같다.
 * 선택 필드 `warnings`는 보내지 않는다. 경고가 있는 결과는 아예 올리지 않기 때문이다.
 */
@Serializable
data class PortalScrapeUploadRequest(
  @SerialName("portal_type") val portalType: String,
  val payload: JsonObject,
)
