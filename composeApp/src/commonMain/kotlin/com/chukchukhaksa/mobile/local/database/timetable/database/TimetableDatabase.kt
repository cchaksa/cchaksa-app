package com.chukchukhaksa.mobile.local.database.timetable.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chukchukhaksa.mobile.local.database.timetable.converter.TimetableCellListConverter
import com.chukchukhaksa.mobile.local.database.timetable.dao.TimeTableDao
import com.chukchukhaksa.mobile.local.database.timetable.entity.TimetableEntity

@Database(
    entities = [TimetableEntity::class],
    version = 2,
)
@TypeConverters(
    value = [TimetableCellListConverter::class],
)
@ConstructedBy(TimetableDatabaseConstructor::class)
abstract class TimetableDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimeTableDao
}
