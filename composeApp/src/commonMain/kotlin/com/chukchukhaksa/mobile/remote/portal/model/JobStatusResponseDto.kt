package com.chukchukhaksa.mobile.remote.portal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobStatusResponseDto(
    @SerialName("job_id") val jobId: String? = null,
    @SerialName("portal_type") val portalType: String? = null,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("finished_at") val finishedAt: String? = null,
    val status: String? = null,
    val retryable: Boolean? = null,
)
