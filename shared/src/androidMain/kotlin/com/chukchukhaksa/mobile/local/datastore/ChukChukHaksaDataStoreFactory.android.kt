package com.chukchukhaksa.mobile.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

actual class ChukChukHaksaDataStoreFactory(
    private val context: Context
) {
    actual fun createChukChukHaksaDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                context.filesDir
                    .resolve(CHUK_CHUK_HAKSA_DATA_STORE_FILE_NAME)
                    .absolutePath
                    .toPath()
            }
        )
}