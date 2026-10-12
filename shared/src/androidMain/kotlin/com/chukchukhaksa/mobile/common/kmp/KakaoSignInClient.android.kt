package com.chukchukhaksa.mobile.common.kmp

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class KakaoSignInClient {

    actual suspend fun signIn(context: Any?): KakaoSignInResult {
        val androidContext = context as? Context
            ?: throw IllegalStateException("Android Context is required for Kakao Login")

        val nonce = UUID.randomUUID().toString()
        val hashedNonce = sha256Hex(nonce)

        return suspendCancellableCoroutine { continuation ->
            val callback: (OAuthToken?, Throwable?) -> Unit = callback@{ token, error ->
                if (error != null) {
                    // 사용자가 카카오계정 로그인 창을 직접 닫은 경우는 실패가 아닌 취소로 전달한다.
                    val mapped = if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        LoginCancelledException()
                    } else {
                        error
                    }
                    continuation.resumeWithException(mapped)
                    return@callback
                }
                val idToken = token?.idToken
                if (idToken == null) {
                    continuation.resumeWithException(
                        IllegalStateException("id_token이 null입니다. 카카오 콘솔에서 OpenID Connect를 활성화하세요.")
                    )
                    return@callback
                }
                continuation.resume(KakaoSignInResult(idToken = idToken, nonce = nonce))
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(androidContext)) {
                UserApiClient.instance.loginWithKakaoTalk(
                    context = androidContext,
                    nonce = hashedNonce,
                ) { token, error ->
                    if (error != null) {
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            UserApiClient.instance.loginWithKakaoAccount(
                                context = androidContext,
                                nonce = hashedNonce,
                                callback = callback,
                            )
                            return@loginWithKakaoTalk
                        }
                        callback(null, error)
                        return@loginWithKakaoTalk
                    }
                    callback(token, null)
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(
                    context = androidContext,
                    nonce = hashedNonce,
                    callback = callback,
                )
            }
        }
    }
}
