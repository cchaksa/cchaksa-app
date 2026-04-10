package com.chukchukhaksa.mobile.data.portal.datasource

import com.chukchukhaksa.mobile.remote.portal.model.AcceptedResponseDto
import com.chukchukhaksa.mobile.remote.portal.model.JobStatusResponseDto
import com.chukchukhaksa.mobile.remote.portal.model.JobSummaryResponseDto

interface PortalRemoteDataSource {
    suspend fun createLinkJob(
        portalType: String,
        username: String,
        password: String,
        idempotencyKey: String,
    ): AcceptedResponseDto

    suspend fun getJobStatus(jobId: String): JobStatusResponseDto

    suspend fun getJobSummary(jobId: String): JobSummaryResponseDto
}
