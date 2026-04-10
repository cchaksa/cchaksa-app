package com.chukchukhaksa.mobile.domain.portal.repository

import com.chukchukhaksa.mobile.domain.portal.model.ScrapingProgress
import kotlinx.coroutines.flow.Flow

interface PortalRepository {
    fun linkPortal(
        portalType: String,
        username: String,
        password: String,
        idempotencyKey: String,
    ): Flow<ScrapingProgress>
}
