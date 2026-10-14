package com.chukchukhaksa.mobile.data.auth.model

data class SignInResult(
    val accessToken: String,
    val refreshToken: String,
    val isPortalLinked: Boolean,
)
