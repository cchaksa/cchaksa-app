package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.local.database.openlecture.database.OpenLectureDatabaseFactory
import com.chukchukhaksa.mobile.local.database.openmajor.database.OpenMajorDatabaseFactory
import com.chukchukhaksa.mobile.local.database.timetable.database.TimetableDatabaseFactory
import com.chukchukhaksa.mobile.local.datastore.ChukChukHaksaDataStoreFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

actual val platformModule = module {
    single { TimetableDatabaseFactory(androidApplication()) }
    single { OpenMajorDatabaseFactory(androidApplication()) }
    single { OpenLectureDatabaseFactory(androidApplication()) }
    single { ChukChukHaksaDataStoreFactory(androidApplication()) }
}
