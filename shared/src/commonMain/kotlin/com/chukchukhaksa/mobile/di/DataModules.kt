package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.data.openmajor.di.openMajorRepositoryModule
import com.chukchukhaksa.mobile.data.timetable.di.timetableRepositoryModule
import com.chukchukhaksa.mobile.local.database.common.di.databaseModule
import com.chukchukhaksa.mobile.local.datasource.openmajor.di.localOpenMajorDataSourceModule
import com.chukchukhaksa.mobile.local.datasource.timetable.di.localTimetableDataSourceModule
import com.chukchukhaksa.mobile.local.datastore.di.dataStoreModule
import org.koin.dsl.module

val dataModule = module {
    includes(
        dataStoreModule,
        databaseModule,
        localTimetableDataSourceModule,
        timetableRepositoryModule,
        openMajorRepositoryModule,
        localOpenMajorDataSourceModule
    )
}
