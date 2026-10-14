package com.chukchukhaksa.mobile.data.auth.repository

import com.chukchukhaksa.mobile.data.auth.model.SignInResult

interface AuthRepository {
    /** 소셜 로그인 토큰으로 서버에 로그인하고, 발급받은 access/refresh token을 저장한다. */
    suspend fun signIn(provider: String, idToken: String, nonce: String): SignInResult

    /**
     * 저장된 refresh token으로 세션을 갱신하고 로그인 상태인지 돌려준다.
     *
     * 서버가 오류 응답([com.chukchukhaksa.mobile.remote.common.ApiException])을 돌려주면 저장된 토큰을
     * 지운다. 네트워크 오류처럼 응답을 받지 못한 실패는 토큰을 남겨 둔 채 `false`를 돌려준다.
     */
    suspend fun restoreSession(): Boolean
    suspend fun clearTokens()
}
