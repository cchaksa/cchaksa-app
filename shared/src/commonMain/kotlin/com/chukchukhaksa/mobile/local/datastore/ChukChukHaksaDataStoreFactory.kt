package com.chukchukhaksa.mobile.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect class ChukChukHaksaDataStoreFactory {
    fun createChukChukHaksaDataStore(): DataStore<Preferences>
}

internal const val CHUK_CHUK_HAKSA_DATA_STORE_FILE_NAME = "chuk-chuk-haksa-preference.preferences_pb"
