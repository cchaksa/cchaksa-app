package com.chukchukhaksa.mobile.remote.config

import com.chukchukhaksa.mobile.data.config.datasource.RemoteAppConfigDataSource
import org.koin.dsl.module

val remoteAppConfigDataSourceModule = module {
  single<RemoteAppConfigDataSource> { RemoteAppConfigDataSourceImpl(get()) }
}
