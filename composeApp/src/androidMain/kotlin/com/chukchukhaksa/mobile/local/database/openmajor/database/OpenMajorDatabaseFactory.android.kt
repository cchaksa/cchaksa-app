package com.chukchukhaksa.mobile.local.database.openmajor.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chukchukhaksa.mobile.local.database.common.DatabaseName

actual class OpenMajorDatabaseFactory(
    private val context: Context
) {
    actual fun create(): RoomDatabase.Builder<OpenMajorDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(DatabaseName.OPEN_MAJOR)

        return Room.databaseBuilder(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}