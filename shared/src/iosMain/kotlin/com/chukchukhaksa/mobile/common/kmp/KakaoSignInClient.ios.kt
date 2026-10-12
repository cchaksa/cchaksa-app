package com.chukchukhaksa.mobile.common.kmp

import dev.yjyoon.kinappbrowser.KInAppBrowser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSUUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class KakaoSignInClient(
    private val bridge: KakaoLoginBridge,
) {
    actual suspend fun signIn(context: Any?): KakaoSignInResult {
        val rawNonce = NSUUID().UUIDString()
        val hashedNonce = sha256Hex(rawNonce)

        return if (bridge.isKakaoTalkAvailable()) {
            try {
                signInWithKakaoTalk(rawNonce = rawNonce, hashedNonce = hashedNonce)
            } catch (e: KakaoTalkCancelledException) {
                signInWithInAppBrowser(rawNonce = rawNonce, hashedNonce = hashedNonce)
            }
        } else {
            signInWithInAppBrowser(rawNonce = rawNonce, hashedNonce = hashedNonce)
        }
    }

    private suspend fun signInWithKakaoTalk(
        rawNonce: String,
        hashedNonce: String,
    ): KakaoSignInResult {
        return suspendCancellableCoroutine { continuation ->
            bridge.loginWithKakaoTalk(
                nonce = hashedNonce,
                onSuccess = { idToken ->
                    continuation.resume(KakaoSignInResult(idToken = idToken, nonce = rawNonce))
                },
                onFailure = { error ->
                    continuation.resumeWithException(error)
                },
            )
        }
    }

    private suspend fun signInWithInAppBrowser(
        rawNonce: String,
        hashedNonce: String,
    ): KakaoSignInResult {
        val oauthInfo = bridge.buildOAuthUrl(hashedNonce)
        val deferred = KakaoOAuthRedirectHandler.prepare()

        try {
            KInAppBrowser.open(oauthInfo.url)

            val code = deferred.await()

            withContext(Dispatchers.Main) {
                KInAppBrowser.close()
            }

            return exchangeCodeForToken(code, oauthInfo.codeVerifier, rawNonce)
        } catch (e: Exception) {
            KakaoOAuthRedirectHandler.cancel()
            withContext(Dispatchers.Main) {
                KInAppBrowser.close()
            }
            throw e
        }
    }

    private suspend fun exchangeCodeForToken(
        code: String,
        codeVerifier: String,
        rawNonce: String,
    ): KakaoSignInResult {
        return suspendCancellableCoroutine { continuation ->
            bridge.exchangeCodeForToken(
                code = code,
                codeVerifier = codeVerifier,
                onSuccess = { idToken ->
                    continuation.resume(KakaoSignInResult(idToken = idToken, nonce = rawNonce))
                },
                onFailure = { error ->
                    continuation.resumeWithException(error)
                },
            )
        }
    }
}
