package com.chukchukhaksa.mobile.data.portal.repository

import com.chukchukhaksa.mobile.data.portal.datasource.PortalRemoteDataSource
import com.chukchukhaksa.mobile.domain.portal.model.PortalScrapingError
import com.chukchukhaksa.mobile.domain.portal.model.PortalScrapingException
import com.chukchukhaksa.mobile.domain.portal.model.ScrapingProgress
import com.chukchukhaksa.mobile.domain.portal.model.ScrapingResult
import com.chukchukhaksa.mobile.domain.portal.model.StudentInfo
import com.chukchukhaksa.mobile.domain.portal.repository.PortalRepository
import com.chukchukhaksa.mobile.remote.portal.mapToPortalScrapingError
import com.chukchukhaksa.mobile.remote.portal.model.JobStatusResponseDto
import com.chukchukhaksa.mobile.remote.portal.model.JobSummaryResponseDto
import com.chukchukhaksa.mobile.remote.portal.model.StudentInfoDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PortalRepositoryImpl(
    private val portalRemoteDataSource: PortalRemoteDataSource,
) : PortalRepository {

    override fun linkPortal(
        portalType: String,
        username: String,
        password: String,
        idempotencyKey: String,
    ): Flow<ScrapingProgress> = flow {
        val accepted = portalRemoteDataSource.createLinkJob(
            portalType = portalType,
            username = username,
            password = password,
            idempotencyKey = idempotencyKey,
        )
        val jobId = accepted.jobId ?: throw PortalScrapingException(
            error = PortalScrapingError.Unknown(httpStatus = null, appCode = null),
            httpStatus = null,
            appCode = null,
            retryable = null,
            message = "포털 연동 job id가 비어있습니다.",
        )

        emit(ScrapingProgress.Accepted(jobId = jobId))

        repeat(MAX_POLL_COUNT) {
            delay(POLL_INTERVAL_MS)

            val statusDto = portalRemoteDataSource.getJobStatus(jobId)
            val jobStatus = statusDto.status.orEmpty()

            when {
                jobStatus.isTerminalSuccess() -> {
                    val summary = portalRemoteDataSource.getJobSummary(jobId)
                    emit(
                        ScrapingProgress.Completed(
                            jobId = jobId,
                            result = summary.toDomain(),
                        ),
                    )
                    return@flow
                }

                jobStatus.isTerminalFailure() -> {
                    throw statusDto.toException()
                }

                else -> emit(
                    ScrapingProgress.InProgress(
                        jobId = jobId,
                        status = jobStatus,
                    ),
                )
            }
        }

        throw PortalScrapingException(
            error = PortalScrapingError.Unknown(httpStatus = null, appCode = TIMEOUT_APP_CODE),
            httpStatus = null,
            appCode = TIMEOUT_APP_CODE,
            retryable = null,
            message = "포털 연동이 시간 내에 완료되지 않았습니다.",
        )
    }

    companion object {
        private const val POLL_INTERVAL_MS = 2_000L
        private const val MAX_POLL_COUNT = 60
        private const val TIMEOUT_APP_CODE = "PORTAL_POLL_TIMEOUT"
    }
}

private fun String.isTerminalSuccess(): Boolean =
    equals("succeeded", ignoreCase = true)

private fun String.isTerminalFailure(): Boolean =
    equals("failed", ignoreCase = true)

private fun JobStatusResponseDto.toException(): PortalScrapingException {
    val appCode = errorCode
    val error = mapToPortalScrapingError(httpStatus = null, appCode = appCode)
    return PortalScrapingException(
        error = error,
        httpStatus = null,
        appCode = appCode,
        retryable = retryable,
        message = errorMessage ?: error.defaultMessage,
    )
}

private fun JobSummaryResponseDto.toDomain(): ScrapingResult = ScrapingResult(
    jobId = jobId,
    studentInfo = studentInfo?.toDomain(),
    status = status,
    finishedAt = finishedAt,
)

private fun StudentInfoDto.toDomain(): StudentInfo = StudentInfo(
    name = name,
    school = school,
    majorName = majorName,
    studentCode = studentCode,
    gradeLevel = gradeLevel,
    status = status,
    completedSemesterType = completedSemesterType,
)
