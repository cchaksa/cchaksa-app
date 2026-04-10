package com.chukchukhaksa.mobile.domain.portal.repository

import com.chukchukhaksa.mobile.domain.portal.model.ScrapingResult

interface PortalRepository {
    suspend fun login(username: String, password: String)
    suspend fun startScraping(): ScrapingResult
    suspend fun refreshScraping(): ScrapingResult
}
