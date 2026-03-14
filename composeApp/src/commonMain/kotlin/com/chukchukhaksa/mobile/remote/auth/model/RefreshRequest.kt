package com.chukchukhaksa.mobile.remote.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)
