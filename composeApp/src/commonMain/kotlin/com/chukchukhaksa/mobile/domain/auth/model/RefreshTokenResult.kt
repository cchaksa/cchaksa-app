package com.chukchukhaksa.mobile.domain.auth.model

data class RefreshTokenResult(
    val accessToken: String,
    val refreshToken: String,
)
