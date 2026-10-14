package com.chukchukhaksa.mobile.remote.config

import com.chukchukhaksa.mobile.data.config.datasource.RemoteAppConfigDataSource
import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first

class RemoteAppConfigDataSourceImpl(
    private val firebaseDatabase: FirebaseDatabase,
) : RemoteAppConfigDataSource {

    override suspend fun getMinVersion(): String = getValue(minVersionKey)

    override suspend fun getStoreUrl(): String = getValue(storeUrlKey)

    private suspend fun getValue(key: String): String = firebaseDatabase
        .reference(key)
        .valueEvents
        .first()
        .value
        .toString()
}
