package com.chukchukhaksa.mobile.local.database.common.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.chukchukhaksa.mobile.local.database.timetable.database.TimetableDatabase
import com.chukchukhaksa.mobile.local.database.timetable.database.TimetableDatabaseFactory
import com.chukchukhaksa.mobile.local.common.database.timetable.migration.TIMETABLE_MIGRATION_1_2
import com.chukchukhaksa.mobile.local.database.openlecture.database.OpenLectureDatabase
import com.chukchukhaksa.mobile.local.database.openlecture.database.OpenLectureDatabaseFactory
import com.chukchukhaksa.mobile.local.database.openmajor.database.OpenMajorDatabase
import com.chukchukhaksa.mobile.local.database.openmajor.database.OpenMajorDatabaseFactory
import org.koin.dsl.module

val databaseModule =
    module {
        includes(
            timetableDatabaseModule,
            openMajorDatabaseModule,
            openLectureDatabaseModule,
        )
    }

val timetableDatabaseModule = module {
    single {
        get<TimetableDatabaseFactory>()
            .create()
            .addMigrations(TIMETABLE_MIGRATION_1_2)
            .fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<TimetableDatabase>().timetableDao() }
}

val openMajorDatabaseModule = module {
    single {
        get<OpenMajorDatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<OpenMajorDatabase>().openMajorDao() }
}

val openLectureDatabaseModule = module {
    single {
        get<OpenLectureDatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<OpenLectureDatabase>().openLectureDao() }
}
