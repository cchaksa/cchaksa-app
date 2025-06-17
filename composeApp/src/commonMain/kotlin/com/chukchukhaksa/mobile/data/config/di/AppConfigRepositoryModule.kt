package com.chukchukhaksa.mobile.data.config.di

import com.chukchukhaksa.mobile.data.config.repository.AppConfigRepositoryImpl
import com.chukchukhaksa.mobile.domain.config.repository.AppConfigRepository
import org.koin.dsl.module

val appConfigRepositoryModule = module {
  single<AppConfigRepository> { AppConfigRepositoryImpl(get()) }
}
