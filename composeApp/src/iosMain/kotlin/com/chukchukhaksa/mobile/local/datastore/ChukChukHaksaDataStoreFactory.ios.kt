package com.chukchukhaksa.mobile.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class ChukChukHaksaDataStoreFactory {
    actual fun createChukChukHaksaDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                getDocumentPath(CHUK_CHUK_HAKSA_DATA_STORE_FILE_NAME).toPath()
            }
        )

    @OptIn(ExperimentalForeignApi::class)
    private fun getDocumentPath(fileName: String): String {
        val directory =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
        return requireNotNull(directory).path + "/$fileName"
    }
}