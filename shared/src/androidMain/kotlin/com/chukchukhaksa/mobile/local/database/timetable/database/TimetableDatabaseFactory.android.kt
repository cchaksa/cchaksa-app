package com.chukchukhaksa.mobile.local.database.timetable.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chukchukhaksa.mobile.local.database.common.DatabaseName

actual class TimetableDatabaseFactory(
    private val context: Context
) {
    actual fun create(): RoomDatabase.Builder<TimetableDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(DatabaseName.TIMETABLE)

        return Room.databaseBuilder(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}