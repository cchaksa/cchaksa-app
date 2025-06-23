package com.chukchukhaksa.mobile.local.database.openmajor.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RenameTable
import androidx.room.migration.AutoMigrationSpec
import com.chukchukhaksa.mobile.local.database.openmajor.dao.OpenMajorDao
import com.chukchukhaksa.mobile.local.database.openmajor.entity.OpenMajorEntity

@Database(
    entities = [OpenMajorEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2,
            spec = OpenMajorDatabase.RenameTableAutoMigration::class),
    ],
    exportSchema = true,
)
@ConstructedBy(OpenMajorDatabaseConstructor::class)
abstract class OpenMajorDatabase : RoomDatabase() {
    abstract fun openMajorDao(): OpenMajorDao

    @RenameTable(fromTableName = "OpenMajorData", toTableName = "OpenMajorEntity")
    class RenameTableAutoMigration : AutoMigrationSpec
}