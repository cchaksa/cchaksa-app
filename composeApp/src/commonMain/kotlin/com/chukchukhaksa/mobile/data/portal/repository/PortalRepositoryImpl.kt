package com.chukchukhaksa.mobile.data.portal.repository

import com.chukchukhaksa.mobile.data.portal.datasource.PortalRemoteDataSource
import com.chukchukhaksa.mobile.domain.portal.model.ScrapingResult
import com.chukchukhaksa.mobile.domain.portal.repository.PortalRepository

class PortalRepositoryImpl(
    private val portalRemoteDataSource: PortalRemoteDataSource,
) : PortalRepository {

    override suspend fun login(username: String, password: String) {
        portalRemoteDataSource.login(username, password)
    }

    override suspend fun startScraping(): ScrapingResult {
        return portalRemoteDataSource.startScraping()
    }

    override suspend fun refreshScraping(): ScrapingResult {
        return portalRemoteDataSource.refreshScraping()
    }
}
