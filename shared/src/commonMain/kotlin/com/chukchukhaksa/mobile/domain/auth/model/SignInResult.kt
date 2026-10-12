package com.chukchukhaksa.mobile.domain.auth.model

data class SignInResult(
    val accessToken: String,
    val refreshToken: String,
    val isPortalLinked: Boolean,
)
