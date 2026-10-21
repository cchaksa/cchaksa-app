package com.chukchukhaksa.mobile.remote.auth

import com.chukchukhaksa.mobile.data.auth.datasource.AuthTokenCache
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

/** Auth 플러그인의 bearer provider가 `loadTokens` 결과를 캐시하므로, 그 캐시를 비워 다음 요청에서 다시 읽게 한다. */
class AuthTokenCacheImpl(
    private val httpClient: HttpClient,
) : AuthTokenCache {

    override fun clear() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
