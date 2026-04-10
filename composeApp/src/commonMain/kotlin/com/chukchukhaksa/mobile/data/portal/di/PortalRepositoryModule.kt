package com.chukchukhaksa.mobile.data.portal.di

import com.chukchukhaksa.mobile.data.portal.datasource.PortalRemoteDataSource
import com.chukchukhaksa.mobile.data.portal.repository.PortalRepositoryImpl
import com.chukchukhaksa.mobile.domain.portal.repository.PortalRepository
import com.chukchukhaksa.mobile.remote.portal.PortalRemoteDataSourceImpl
import org.koin.dsl.module

val portalRepositoryModule = module {
    single<PortalRemoteDataSource> { PortalRemoteDataSourceImpl(get()) }
    single<PortalRepository> { PortalRepositoryImpl(get()) }
}
