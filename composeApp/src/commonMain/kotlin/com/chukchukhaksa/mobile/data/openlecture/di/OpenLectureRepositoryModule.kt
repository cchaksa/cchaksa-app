package com.chukchukhaksa.mobile.data.openlecture.di

import com.chukchukhaksa.mobile.domain.timetable.repository.OpenLectureRepository
import com.chukchukhaksa.mobile.data.openlecture.repository.OpenLectureRepositoryImpl
import org.koin.dsl.module

val openLectureRepositoryModule = module {
  single<OpenLectureRepository> { OpenLectureRepositoryImpl(get(), get()) }
}
