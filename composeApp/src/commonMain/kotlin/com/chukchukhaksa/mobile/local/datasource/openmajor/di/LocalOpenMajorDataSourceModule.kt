package com.chukchukhaksa.mobile.local.datasource.openmajor.di

import com.chukchukhaksa.mobile.data.openmajor.datasource.LocalOpenMajorDataSource
import com.chukchukhaksa.mobile.local.datasource.openmajor.datasource.LocalOpenMajorDataSourceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val localOpenMajorDataSourceModule = module {
  single<LocalOpenMajorDataSource> {
    LocalOpenMajorDataSourceImpl(get(named("normalDataStore")), get())
  }
}

