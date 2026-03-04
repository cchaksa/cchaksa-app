package com.chukchukhaksa.mobile.local.datasource.auth.datasource

import com.chukchukhaksa.mobile.data.auth.datasource.LocalAuthDataSource
import eu.anifantakis.lib.ksafe.KSafe

class LocalAuthDataSourceImpl(
    private val ksafe: KSafe,
) : LocalAuthDataSource {

    override suspend fun saveAccessToken(token: String) {
        ksafe.put(KEY_ACCESS_TOKEN, token, encrypted = true)
    }

    override suspend fun getAccessToken(): String? {
        return ksafe.get(KEY_ACCESS_TOKEN, "", encrypted = true).ifEmpty { null }
    }

    override suspend fun saveRefreshToken(token: String) {
        ksafe.put(KEY_REFRESH_TOKEN, token, encrypted = true)
    }

    override suspend fun getRefreshToken(): String? {
        return ksafe.get(KEY_REFRESH_TOKEN, "", encrypted = true).ifEmpty { null }
    }

    override suspend fun clearTokens() {
        ksafe.delete(KEY_ACCESS_TOKEN)
        ksafe.delete(KEY_REFRESH_TOKEN)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
    }
}
