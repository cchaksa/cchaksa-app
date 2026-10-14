package com.chukchukhaksa.mobile.data.openmajor.di

import com.chukchukhaksa.mobile.data.openmajor.repository.OpenMajorRepository
import com.chukchukhaksa.mobile.data.openmajor.repository.OpenMajorRepositoryImpl
import org.koin.dsl.module

val openMajorRepositoryModule = module {
    single<OpenMajorRepository> { OpenMajorRepositoryImpl(get(), get()) }
}

