package com.chukchukhaksa.mobile.remote.timetable

import com.chukchukhaksa.mobile.data.openlecture.datasource.RemoteOpenLectureDataSource
import org.koin.dsl.module

val remoteOpenLectureDataSourceModule = module {
  single<RemoteOpenLectureDataSource> { RemoteOpenLectureDataSourceImpl(get()) }
}
