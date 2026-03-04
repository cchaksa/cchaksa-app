package com.chukchukhaksa.mobile.data.auth.di

import com.chukchukhaksa.mobile.data.auth.datasource.RemoteAuthDataSource
import com.chukchukhaksa.mobile.data.auth.repository.AuthRepositoryImpl
import com.chukchukhaksa.mobile.domain.auth.repository.AuthRepository
import com.chukchukhaksa.mobile.remote.auth.RemoteAuthDataSourceImpl
import org.koin.dsl.module

val authRepositoryModule = module {
    single<RemoteAuthDataSource> { RemoteAuthDataSourceImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}
