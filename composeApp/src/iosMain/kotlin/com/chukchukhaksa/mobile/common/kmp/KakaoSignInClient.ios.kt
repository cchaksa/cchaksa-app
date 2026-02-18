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
        val nonce = NSUUID().UUIDString()

        return if (bridge.isKakaoTalkAvailable()) {
            try {
                signInWithKakaoTalk(nonce)
            } catch (e: KakaoTalkCancelledException) {
                signInWithInAppBrowser(nonce)
            }
        } else {
            signInWithInAppBrowser(nonce)
        }
    }

    private suspend fun signInWithKakaoTalk(nonce: String): KakaoSignInResult {
        return suspendCancellableCoroutine { continuation ->
            bridge.loginWithKakaoTalk(
                nonce = nonce,
                onSuccess = { idToken ->
                    continuation.resume(KakaoSignInResult(idToken = idToken, nonce = nonce))
                },
                onFailure = { error ->
                    continuation.resumeWithException(error)
                },
            )
        }
    }

    private suspend fun signInWithInAppBrowser(nonce: String): KakaoSignInResult {
        val oauthInfo = bridge.buildOAuthUrl(nonce)
        val deferred = KakaoOAuthRedirectHandler.prepare()

        try {
            KInAppBrowser.open(oauthInfo.url)

            val code = deferred.await()

            withContext(Dispatchers.Main) {
                KInAppBrowser.close()
            }

            return exchangeCodeForToken(code, oauthInfo.codeVerifier, nonce)
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
        nonce: String,
    ): KakaoSignInResult {
        return suspendCancellableCoroutine { continuation ->
            bridge.exchangeCodeForToken(
                code = code,
                codeVerifier = codeVerifier,
                onSuccess = { idToken ->
                    continuation.resume(KakaoSignInResult(idToken = idToken, nonce = nonce))
                },
                onFailure = { error ->
                    continuation.resumeWithException(error)
                },
            )
        }
    }
}
