package com.chukchukhaksa.mobile.common.kmp

data class KakaoOAuthUrlInfo(val url: String, val codeVerifier: String)

class KakaoTalkCancelledException : Exception("KakaoTalk login was cancelled by user")

interface KakaoLoginBridge {
    fun isKakaoTalkAvailable(): Boolean
    fun loginWithKakaoTalk(
        nonce: String,
        onSuccess: (idToken: String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
    fun buildOAuthUrl(nonce: String): KakaoOAuthUrlInfo
    fun exchangeCodeForToken(
        code: String,
        codeVerifier: String,
        onSuccess: (idToken: String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}
