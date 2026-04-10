package com.chukchukhaksa.mobile.remote.portal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcceptedResponseDto(
    @SerialName("job_id") val jobId: String? = null,
    @SerialName("polling_endpoint") val pollingEndpoint: String? = null,
    val status: String? = null,
)
