package com.chukchukhaksa.mobile.data.timetable.di

import com.chukchukhaksa.mobile.data.timetable.repository.TimetableRepository
import com.chukchukhaksa.mobile.data.timetable.repository.TimetableRepositoryImpl
import org.koin.dsl.module

val timetableRepositoryModule = module {
    single<TimetableRepository> { TimetableRepositoryImpl(get()) }
}
