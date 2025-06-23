package com.chukchukhaksa.mobile.domain.config.usecase

import com.chukchukhaksa.mobile.common.kmp.Platform
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.config.repository.AppConfigRepository

class CheckNeedForceUpdateUseCase(
    private val appConfigRepository: AppConfigRepository
) {
    suspend operator fun invoke(platform: Platform, currentVersion: String): Result<ForceUpdateInfo> = runCatchingIgnoreCancelled {
        val minVersion = when (platform) {
            Platform.Android -> appConfigRepository.getAndroidMinVersion()
            Platform.IOS -> appConfigRepository.getIOSMinVersion()
        }
        
        val needForceUpdate = compareVersions(currentVersion, minVersion) < 0

        if (needForceUpdate.not()) {
            return@runCatchingIgnoreCancelled ForceUpdateInfo(needForceUpdate = false)
        }

        val storeUrl = when (platform) {
            Platform.Android -> appConfigRepository.getGoogleStoreUrl()
            Platform.IOS -> appConfigRepository.getAppleStoreUrl()
        }

        ForceUpdateInfo(needForceUpdate = true, storeUrl = storeUrl)
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

    data class ForceUpdateInfo(
        val needForceUpdate: Boolean,
        val storeUrl: String? = null,
    )
}