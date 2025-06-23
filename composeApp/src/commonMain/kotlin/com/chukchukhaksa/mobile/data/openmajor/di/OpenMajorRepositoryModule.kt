package com.chukchukhaksa.mobile.data.openmajor.di

import com.chukchukhaksa.mobile.data.openmajor.repository.OpenMajorRepositoryImpl
import com.chukchukhaksa.mobile.domain.openmajor.repository.OpenMajorRepository
import org.koin.dsl.module

val openMajorRepositoryModule = module {
    single<OpenMajorRepository> { OpenMajorRepositoryImpl(get(), get()) }
}

