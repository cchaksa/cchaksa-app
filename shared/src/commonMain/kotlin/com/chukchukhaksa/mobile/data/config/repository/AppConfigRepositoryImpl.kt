package com.chukchukhaksa.mobile.data.config.repository

import com.chukchukhaksa.mobile.data.config.datasource.RemoteAppConfigDataSource
import com.chukchukhaksa.mobile.data.config.model.ForceUpdateInfo

class AppConfigRepositoryImpl(
    private val remoteAppConfigDataSource: RemoteAppConfigDataSource,
) : AppConfigRepository {
    override suspend fun getForceUpdateInfo(currentVersion: String): ForceUpdateInfo {
        val minVersion = remoteAppConfigDataSource.getMinVersion()

        val needForceUpdate = compareVersions(currentVersion, minVersion) < 0

        if (needForceUpdate.not()) {
            return ForceUpdateInfo(needForceUpdate = false)
        }

        return ForceUpdateInfo(needForceUpdate = true, storeUrl = remoteAppConfigDataSource.getStoreUrl())
    }

    private fun compareVersions(currentVersion: String, minVersion: String): Int {
        val currentParts = currentVersion.split(".").map { it.toInt() }
        val minimumParts = minVersion.split(".").map { it.toInt() }

        val maxLength = maxOf(currentParts.size, minimumParts.size)

        for (i in 0 until maxLength) {
            val currentPart = currentParts.getOrElse(i) { 0 }
            val minimumPart = minimumParts.getOrElse(i) { 0 }

            when {
                currentPart < minimumPart -> return -1
                currentPart > minimumPart -> return 1
            }
        }

        return 0
    }
}
