package com.chukchukhaksa.mobile.data.portalscrape.di

import com.chukchukhaksa.mobile.data.portalscrape.datasource.RemotePortalScrapeDataSource
import com.chukchukhaksa.mobile.data.portalscrape.repository.PortalScrapeRepositoryImpl
import com.chukchukhaksa.mobile.domain.portalscrape.repository.PortalScrapeRepository
import com.chukchukhaksa.mobile.remote.portalscrape.RemotePortalScrapeDataSourceImpl
import org.koin.dsl.module

val portalScrapeRepositoryModule = module {
  single<RemotePortalScrapeDataSource> { RemotePortalScrapeDataSourceImpl() }
  single<PortalScrapeRepository> { PortalScrapeRepositoryImpl(get()) }
}
