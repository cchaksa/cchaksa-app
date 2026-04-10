package com.chukchukhaksa.mobile.remote.portal

import com.chukchukhaksa.mobile.data.portal.datasource.PortalRemoteDataSource
import com.chukchukhaksa.mobile.domain.portal.model.PortalScrapingException
import com.chukchukhaksa.mobile.domain.portal.model.ScrapingResult
import com.chukchukhaksa.mobile.domain.portal.model.StudentInfo
import com.chukchukhaksa.mobile.remote.common.ApiResponse
import com.chukchukhaksa.mobile.remote.portal.model.ScrapingResponseDto
import com.chukchukhaksa.mobile.remote.portal.model.StudentInfoDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.JsonObject

class PortalRemoteDataSourceImpl(
    private val httpClient: HttpClient,
) : PortalRemoteDataSource {

    override suspend fun login(username: String, password: String) {
        val httpResponse = httpClient.post("suwon-scrape/login") {
            parameter("username", username)
            parameter("password", password)
        }
        parseOrThrow<JsonObject>(httpResponse)
    }

    override suspend fun startScraping(): ScrapingResult {
        val httpResponse = httpClient.post("suwon-scrape/start")
        val dto = parseOrThrow<ScrapingResponseDto>(httpResponse)
        return dto.toDomain()
    }

    override suspend fun refreshScraping(): ScrapingResult {
        val httpResponse = httpClient.post("suwon-scrape/refresh")
        val dto = parseOrThrow<ScrapingResponseDto>(httpResponse)
        return dto.toDomain()
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
}

private fun ScrapingResponseDto.toDomain() = ScrapingResult(
    taskId = taskId,
    studentInfo = studentInfo?.toDomain(),
    status = status,
)

private fun StudentInfoDto.toDomain() = StudentInfo(
    name = name,
    school = school,
    majorName = majorName,
    studentCode = studentCode,
    gradeLevel = gradeLevel,
    status = status,
    completedSemesterType = completedSemesterType,
)
