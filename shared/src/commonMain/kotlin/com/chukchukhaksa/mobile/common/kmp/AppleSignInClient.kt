package com.chukchukhaksa.mobile.common.kmp

data class AppleSignInResult(
    val identityToken: String,
    val authorizationCode: String,
    val userId: String,
    val email: String?,
    val fullName: String?,
    val nonce: String,
)

expect class AppleSignInClient() {
    suspend fun signIn(): AppleSignInResult
}
