package com.chukchukhaksa.mobile.data.portal.datasource

import com.chukchukhaksa.mobile.domain.portal.model.ScrapingResult

interface PortalRemoteDataSource {
    suspend fun login(username: String, password: String)
    suspend fun startScraping(): ScrapingResult
    suspend fun refreshScraping(): ScrapingResult
}
