package com.chukchukhaksa.mobile.local.datasource.openlecture.di

import com.chukchukhaksa.mobile.data.openlecture.datasource.LocalOpenLectureDataSource
import com.chukchukhaksa.mobile.local.datasource.openlecture.datasource.LocalOpenLectureDatasourceImpl
import org.koin.dsl.module

val localOpenLectureDataSourceModule = module {
    single<LocalOpenLectureDataSource> {
        LocalOpenLectureDatasourceImpl(get(), get())
    }
}
