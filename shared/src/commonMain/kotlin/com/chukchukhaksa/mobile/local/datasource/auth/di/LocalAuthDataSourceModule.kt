package com.chukchukhaksa.mobile.local.datasource.auth.di

import com.chukchukhaksa.mobile.data.auth.datasource.LocalAuthDataSource
import com.chukchukhaksa.mobile.local.datasource.auth.datasource.LocalAuthDataSourceImpl
import org.koin.dsl.module

val localAuthDataSourceModule = module {
    single<LocalAuthDataSource> { LocalAuthDataSourceImpl(get()) }
}
