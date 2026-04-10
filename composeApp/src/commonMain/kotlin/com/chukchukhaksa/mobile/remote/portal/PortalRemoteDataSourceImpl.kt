package com.chukchukhaksa.mobile.remote.portal

import com.chukchukhaksa.mobile.data.portal.datasource.PortalRemoteDataSource
import com.chukchukhaksa.mobile.domain.portal.model.PortalScrapingException
import com.chukchukhaksa.mobile.remote.common.ApiResponse
import com.chukchukhaksa.mobile.remote.portal.model.AcceptedResponseDto
import com.chukchukhaksa.mobile.remote.portal.model.JobStatusResponseDto
import com.chukchukhaksa.mobile.remote.portal.model.JobSummaryResponseDto
import com.chukchukhaksa.mobile.remote.portal.model.LinkRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path

class PortalRemoteDataSourceImpl(
    private val httpClient: HttpClient,
) : PortalRemoteDataSource {

    override suspend fun createLinkJob(
        portalType: String,
        username: String,
        password: String,
        idempotencyKey: String,
    ): AcceptedResponseDto {
        val httpResponse = httpClient.post {
            url { path("portal", "link") }
            header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
            contentType(ContentType.Application.Json)
            setBody(
                LinkRequestDto(
                    portalType = portalType,
                    username = username,
                    password = password,
                ),
            )
        }
        return parseOrThrow<AcceptedResponseDto>(httpResponse)
    }

    override suspend fun getJobStatus(jobId: String): JobStatusResponseDto {
        val httpResponse = httpClient.get {
            url { path("portal", "link", "jobs", jobId) }
        }
        return parseOrThrow<JobStatusResponseDto>(httpResponse)
    }

    override suspend fun getJobSummary(jobId: String): JobSummaryResponseDto {
        val httpResponse = httpClient.get {
            url { path("portal", "link", "jobs", jobId, "summary") }
        }
        return parseOrThrow<JobSummaryResponseDto>(httpResponse)
    }

    private suspend inline fun <reified T> parseOrThrow(httpResponse: HttpResponse): T {
        val status = httpResponse.status.value
        val apiResponse = runCatching {
            httpResponse.body<ApiResponse<T>>()
        }.getOrElse { cause ->
            throw PortalScrapingException(
                error = mapToPortalScrapingError(httpStatus = status, appCode = null),
                httpStatus = status,
                appCode = null,
                message = cause.message ?: "응답 파싱에 실패했습니다.",
            )
        }

        val data = apiResponse.data
        if (apiResponse.success && data != null) {
            return data
        }

        val appCode = apiResponse.error?.code
        val error = mapToPortalScrapingError(httpStatus = status, appCode = appCode)
        throw PortalScrapingException(
            error = error,
            httpStatus = status,
            appCode = appCode,
            message = apiResponse.error?.message ?: error.defaultMessage,
        )
    }

    companion object {
        private const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"
    }
}
