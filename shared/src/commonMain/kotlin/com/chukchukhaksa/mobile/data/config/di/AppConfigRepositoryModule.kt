package com.chukchukhaksa.mobile.data.config.di

import com.chukchukhaksa.mobile.data.config.repository.AppConfigRepository
import com.chukchukhaksa.mobile.data.config.repository.AppConfigRepositoryImpl
import org.koin.dsl.module

val appConfigRepositoryModule = module {
  single<AppConfigRepository> { AppConfigRepositoryImpl(get()) }
}
