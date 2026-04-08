package com.chukchukhaksa.mobile.data.graduation.di

import com.chukchukhaksa.mobile.data.graduation.datasource.RemoteGraduationDataSource
import com.chukchukhaksa.mobile.data.graduation.repository.GraduationRepositoryImpl
import com.chukchukhaksa.mobile.domain.graduation.repository.GraduationRepository
import com.chukchukhaksa.mobile.remote.graduation.RemoteGraduationDataSourceImpl
import org.koin.dsl.module

val GraduationRepositoryModule = module {
    single<RemoteGraduationDataSource> { RemoteGraduationDataSourceImpl() }
    single<GraduationRepository> { GraduationRepositoryImpl(get()) }
}
