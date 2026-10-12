package com.chukchukhaksa.mobile.remote.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class SignInResponse(
    val accessToken: String,
    val refreshToken: String,
    val isPortalLinked: Boolean,
)
