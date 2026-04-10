package com.chukchukhaksa.mobile.remote.portal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkRequestDto(
    @SerialName("portal_type") val portalType: String,
    val username: String,
    val password: String,
)
