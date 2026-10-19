package com.chukchukhaksa.mobile.data.portallink.di

import com.chukchukhaksa.mobile.data.portallink.datasource.RemotePortalLinkDataSource
import com.chukchukhaksa.mobile.data.portallink.debug.PortalLinkDebugSettings
import com.chukchukhaksa.mobile.data.portallink.repository.PortalLinkRepository
import com.chukchukhaksa.mobile.data.portallink.repository.PortalLinkRepositoryImpl
import com.chukchukhaksa.mobile.data.portalscrape.datasource.RemotePortalScrapeDataSource
import com.chukchukhaksa.mobile.remote.portallink.RemotePortalLinkDataSourceImpl
import com.chukchukhaksa.mobile.remote.portalscrape.RemotePortalScrapeDataSourceImpl
import org.koin.dsl.module

val portalLinkRepositoryModule = module {
  single { PortalLinkDebugSettings() }
  single<RemotePortalScrapeDataSource> { RemotePortalScrapeDataSourceImpl() }
  single<RemotePortalLinkDataSource> { RemotePortalLinkDataSourceImpl(client = get()) }
  single<PortalLinkRepository> { PortalLinkRepositoryImpl(get(), get(), debugSettings = get()) }
}
