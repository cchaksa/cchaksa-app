package com.chukchukhaksa.mobile.data.config.repository

import com.chukchukhaksa.mobile.data.config.datasource.RemoteAppConfigDataSource
import com.chukchukhaksa.mobile.domain.config.repository.AppConfigRepository

class AppConfigRepositoryImpl(
    private val remoteAppConfigDataSource: RemoteAppConfigDataSource,
) : AppConfigRepository {
    override suspend fun getAndroidMinVersion(): String {
        return remoteAppConfigDataSource.getAndroidMinVersion()
    }

    override suspend fun getIOSMinVersion(): String {
        return remoteAppConfigDataSource.getIOSMinVersion()
    }

    override suspend fun getAppleStoreUrl(): String {
        return remoteAppConfigDataSource.getAppleStoreUrl()
    }

    override suspend fun getGoogleStoreUrl(): String {
        return remoteAppConfigDataSource.getGoogleStoreUrl()
    }
}
