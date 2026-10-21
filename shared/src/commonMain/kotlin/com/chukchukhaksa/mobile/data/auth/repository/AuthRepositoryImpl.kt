package com.chukchukhaksa.mobile.data.auth.repository

import com.chukchukhaksa.mobile.data.auth.datasource.AuthTokenCache
import com.chukchukhaksa.mobile.data.auth.datasource.LocalAuthDataSource
import com.chukchukhaksa.mobile.data.auth.datasource.RemoteAuthDataSource
import com.chukchukhaksa.mobile.data.auth.model.SignInResult
import com.chukchukhaksa.mobile.remote.common.ApiException
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val localAuthDataSource: LocalAuthDataSource,
    private val authTokenCache: AuthTokenCache,
) : AuthRepository {

    override suspend fun signIn(provider: String, idToken: String, nonce: String): SignInResult {
        val result = remoteAuthDataSource.signIn(provider = provider, idToken = idToken, nonce = nonce)
        saveTokens(accessToken = result.accessToken, refreshToken = result.refreshToken)
        return result
    }

    override suspend fun restoreSession(): Boolean {
        return try {
            val token = localAuthDataSource.getRefreshToken() ?: return false
            val result = remoteAuthDataSource.refreshToken(token)
            saveTokens(accessToken = result.accessToken, refreshToken = result.refreshToken)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            clearTokens()
            false
        } catch (e: Exception) {
            false
        }
    }

    // 저장된 토큰을 바꾼 뒤에는 HTTP 클라이언트의 캐시도 비운다. 비우지 않으면 탈퇴 후 재가입한 계정의 요청에
    // 이전 계정의 토큰이 계속 붙는다.
    override suspend fun clearTokens() {
        localAuthDataSource.clearTokens()
        authTokenCache.clear()
    }

    private suspend fun saveTokens(accessToken: String, refreshToken: String) {
        localAuthDataSource.saveAccessToken(accessToken)
        localAuthDataSource.saveRefreshToken(refreshToken)
        authTokenCache.clear()
    }
}
