package com.chukchukhaksa.mobile.remote.portallink

import com.chukchukhaksa.mobile.data.portallink.datasource.RemotePortalLinkDataSource
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJob
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJobStatus
import com.chukchukhaksa.mobile.remote.common.ApiException
import com.chukchukhaksa.mobile.remote.common.ApiResponse
import com.chukchukhaksa.mobile.remote.common.getDataOrThrow
import com.chukchukhaksa.mobile.remote.portallink.dto.PortalLinkAcceptedResponseData
import com.chukchukhaksa.mobile.remote.portallink.dto.PortalLinkJobResponseData
import com.chukchukhaksa.mobile.remote.portallink.dto.PortalLinkJobSummaryResponseData
import com.chukchukhaksa.mobile.remote.portallink.dto.PortalLinkRequest
import com.chukchukhaksa.mobile.remote.portallink.dto.PortalScrapeUploadRequest
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

/**
 * 포털 연동 API는 다른 API와 달리 `/api` 아래가 아니라 호스트 바로 아래(`/portal/link`)에 있다.
 * 기본 URL이 `.../api/`이므로 경로를 `/`로 시작해 기본 경로를 덮어쓴다.
 */
class RemotePortalLinkDataSourceImpl(
  private val client: HttpClient,
) : RemotePortalLinkDataSource {

  override suspend fun uploadScrapeResult(rawJson: String, idempotencyKey: String) {
    Napier.d(tag = LOG_TAG) { "POST $SCRAPE_RESULTS_PATH" }
    val response = client.post(SCRAPE_RESULTS_PATH) {
      // 서버는 저장을 마친 뒤에 응답한다. 앱 기본 제한 시간(15초)에 걸리면 서버가 아직 저장하는 중에
      // 같은 결과를 다시 보내게 되므로 이 요청만 더 오래 기다린다.
      timeout {
        requestTimeoutMillis = UPLOAD_TIMEOUT_MILLIS
        socketTimeoutMillis = UPLOAD_TIMEOUT_MILLIS
      }
      header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
      setBody(PortalScrapeUploadRequest(portalType = PORTAL_TYPE, payload = Json.parseToJsonElement(rawJson).jsonObject))
    }
    // 5xx는 서버가 결과를 보고 거부한 것이 아니다. 본문에 오류 코드가 있어도 ApiException으로 바꾸지 않아야
    // 호출하는 쪽이 같은 결과를 다시 보낸다.
    if (response.status.value >= 500) {
      throw ServerResponseException(response, response.bodyAsText())
    }
    val body = response.body<ApiResponse<JsonElement>>()
    if (!body.success) {
      throw ApiException(
        code = body.error?.code.orEmpty(),
        message = body.error?.message ?: "학업 정보를 저장하지 못했어요.",
      )
    }
  }

  override suspend fun createLinkJob(studentId: String, password: String, idempotencyKey: String): String {
    Napier.d(tag = LOG_TAG) { "POST $LINK_PATH" }
    return client.post(LINK_PATH) {
      header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
      setBody(PortalLinkRequest(portalType = PORTAL_TYPE, username = studentId, password = password))
    }.body<ApiResponse<PortalLinkAcceptedResponseData>>()
      .getDataOrThrow()
      .jobId
  }

  override suspend fun getLinkJob(jobId: String): PortalLinkJob {
    val data = client.get(jobPath(jobId))
      .body<ApiResponse<PortalLinkJobResponseData>>()
      .getDataOrThrow()
    return PortalLinkJob(status = data.status.toJobStatus(), errorCode = data.errorCode)
  }

  override suspend fun getLinkJobSummaryStatus(jobId: String): PortalLinkJobStatus =
    client.get("${jobPath(jobId)}/summary")
      .body<ApiResponse<PortalLinkJobSummaryResponseData>>()
      .getDataOrThrow()
      .status
      .toJobStatus()

  private fun jobPath(jobId: String): String = "$LINK_PATH/jobs/$jobId"

  private fun String?.toJobStatus(): PortalLinkJobStatus = when (this) {
    "succeeded" -> PortalLinkJobStatus.SUCCEEDED
    "failed" -> PortalLinkJobStatus.FAILED
    else -> PortalLinkJobStatus.IN_PROGRESS
  }

  private companion object {
    const val LOG_TAG = "PortalLinkApi"
    const val PORTAL_TYPE = "suwon"
    const val LINK_PATH = "/portal/link"
    const val SCRAPE_RESULTS_PATH = "/portal/link/scrape-results"
    const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"
    const val UPLOAD_TIMEOUT_MILLIS = 60_000L
  }
}
