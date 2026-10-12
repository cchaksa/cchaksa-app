package com.chukchukhaksa.mobile.local.datasource.timetable.di

import com.chukchukhaksa.mobile.data.timetable.datasource.LocalTimetableDataSource
import com.chukchukhaksa.mobile.local.datasource.timetable.datasource.LocalTimetableDatasourceImpl
import org.koin.dsl.module

val localTimetableDataSourceModule = module {
    single<LocalTimetableDataSource> {
        LocalTimetableDatasourceImpl(get(), get())
    }
}
