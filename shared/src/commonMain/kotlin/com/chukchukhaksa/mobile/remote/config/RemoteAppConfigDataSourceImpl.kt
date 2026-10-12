package com.chukchukhaksa.mobile.remote.config

import com.chukchukhaksa.mobile.data.config.datasource.RemoteAppConfigDataSource
import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first

class RemoteAppConfigDataSourceImpl(
    private val firebaseDatabase: FirebaseDatabase,
) : RemoteAppConfigDataSource {

    override suspend fun getAndroidMinVersion(): String = firebaseDatabase
        .reference(DATABASE_MIN_ANDROID_VERSION)
        .valueEvents
        .first()
        .value
        .toString()

    override suspend fun getIOSMinVersion(): String = firebaseDatabase
        .reference(DATABASE_MIN_IOS_VERSION)
        .valueEvents
        .first()
        .value
        .toString()

    override suspend fun getAppleStoreUrl(): String = firebaseDatabase
        .reference(DATABASE_APPLE_STORE_URL)
        .valueEvents
        .first()
        .value
        .toString()

    override suspend fun getGoogleStoreUrl(): String = firebaseDatabase
        .reference(DATABASE_GOOGLE_STORE_URL)
        .valueEvents
        .first()
        .value
        .toString()

    companion object {
        private const val DATABASE_MIN_IOS_VERSION = "minIOSVersion"
        private const val DATABASE_MIN_ANDROID_VERSION = "minAndroidVersion"
        private const val DATABASE_APPLE_STORE_URL = "appleStoreUrl"
        private const val DATABASE_GOOGLE_STORE_URL = "googleStoreUrl"
    }
}
