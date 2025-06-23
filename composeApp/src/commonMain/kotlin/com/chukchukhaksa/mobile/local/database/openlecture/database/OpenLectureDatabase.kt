package com.chukchukhaksa.mobile.local.database.openlecture.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chukchukhaksa.mobile.local.database.openlecture.converter.OpenLectureConverter
import com.chukchukhaksa.mobile.local.database.openlecture.dao.OpenLectureDao
import com.chukchukhaksa.mobile.local.database.openlecture.entity.OpenLectureEntity
import com.chukchukhaksa.mobile.local.database.openmajor.database.OpenMajorDatabaseConstructor

@Database(
    entities = [OpenLectureEntity::class],
    version = 2
)

@TypeConverters(
    OpenLectureConverter::class
)

@ConstructedBy(OpenLectureDatabaseConstructor::class)
abstract class OpenLectureDatabase : RoomDatabase() {
    abstract fun openLectureDao(): OpenLectureDao
}