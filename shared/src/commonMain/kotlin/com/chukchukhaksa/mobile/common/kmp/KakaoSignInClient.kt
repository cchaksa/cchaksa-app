package com.chukchukhaksa.mobile.common.kmp

data class KakaoSignInResult(
    val idToken: String,
    val nonce: String,
)

expect class KakaoSignInClient {
    suspend fun signIn(context: Any? = null): KakaoSignInResult
}
