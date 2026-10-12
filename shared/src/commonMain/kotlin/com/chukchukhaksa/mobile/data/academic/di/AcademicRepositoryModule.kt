package com.chukchukhaksa.mobile.data.academic.di

import com.chukchukhaksa.mobile.data.academic.datasource.RemoteAcademicDataSource
import com.chukchukhaksa.mobile.data.academic.repository.AcademicRepositoryImpl
import com.chukchukhaksa.mobile.domain.academic.repository.AcademicRepository
import com.chukchukhaksa.mobile.remote.academic.RemoteAcademicDataSourceImpl
import org.koin.dsl.module

val AcademicRepositoryModule = module {
    single<RemoteAcademicDataSource> { RemoteAcademicDataSourceImpl() }
    single<AcademicRepository> { AcademicRepositoryImpl(get()) }
}
