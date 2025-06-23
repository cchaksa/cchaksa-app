package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.data.config.di.appConfigRepositoryModule
import com.chukchukhaksa.mobile.data.openlecture.di.openLectureRepositoryModule
import com.chukchukhaksa.mobile.data.openmajor.di.openMajorRepositoryModule
import com.chukchukhaksa.mobile.data.timetable.di.timetableRepositoryModule
import com.chukchukhaksa.mobile.local.database.common.di.openLectureDatabaseModule
import com.chukchukhaksa.mobile.local.database.common.di.openMajorDatabaseModule
import com.chukchukhaksa.mobile.local.database.common.di.timetableDatabaseModule
import com.chukchukhaksa.mobile.local.datasource.openlecture.di.localOpenLectureDataSourceModule
import com.chukchukhaksa.mobile.local.datasource.openmajor.di.localOpenMajorDataSourceModule
import com.chukchukhaksa.mobile.local.datasource.timetable.di.localTimetableDataSourceModule
import com.chukchukhaksa.mobile.local.datastore.di.dataStoreModule
import com.chukchukhaksa.mobile.remote.config.remoteAppConfigDataSourceModule
import com.chukchukhaksa.mobile.remote.di.firebaseDatabaseModule
import com.chukchukhaksa.mobile.remote.timetable.remoteOpenLectureDataSourceModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            platformModule,
            domainModule,
            dataStoreModule,
            timetableDatabaseModule,
            openMajorDatabaseModule,
            openLectureDatabaseModule,
            localTimetableDataSourceModule,
            remoteOpenLectureDataSourceModule,
            timetableRepositoryModule,
            localOpenMajorDataSourceModule,
            openMajorRepositoryModule,
            localOpenLectureDataSourceModule,
            openLectureRepositoryModule,
            presentationModule,
            firebaseDatabaseModule,
            remoteAppConfigDataSourceModule,
            appConfigRepositoryModule,
        )
    }
}
