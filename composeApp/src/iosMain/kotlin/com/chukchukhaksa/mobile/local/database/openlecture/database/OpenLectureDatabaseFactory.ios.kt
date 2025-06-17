package com.chukchukhaksa.mobile.local.database.openlecture.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.chukchukhaksa.mobile.local.database.common.DatabaseName
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class OpenLectureDatabaseFactory {
    actual fun create(): RoomDatabase.Builder<OpenLectureDatabase> {
        val dbFilePath = documentDirectory() + "/${DatabaseName.OPEN_LECTURE}"
        return Room
            .databaseBuilder<OpenLectureDatabase>(
                name = dbFilePath
            ).setDriver(BundledSQLiteDriver())
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
        return requireNotNull(documentDirectory?.path)
    }
}