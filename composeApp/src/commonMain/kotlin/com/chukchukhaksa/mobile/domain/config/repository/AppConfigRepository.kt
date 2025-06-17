package com.chukchukhaksa.mobile.domain.config.repository

interface AppConfigRepository {
    suspend fun getAndroidMinVersion(): String
    suspend fun getIOSMinVersion(): String
    suspend fun getAppleStoreUrl(): String
    suspend fun getGoogleStoreUrl(): String
}