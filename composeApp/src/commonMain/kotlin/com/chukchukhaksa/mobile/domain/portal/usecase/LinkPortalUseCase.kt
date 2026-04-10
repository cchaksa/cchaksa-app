package com.chukchukhaksa.mobile.domain.portal.usecase

import com.chukchukhaksa.mobile.domain.portal.model.ScrapingProgress
import com.chukchukhaksa.mobile.domain.portal.repository.PortalRepository
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LinkPortalUseCase(
    private val portalRepository: PortalRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    operator fun invoke(
        username: String,
        password: String,
        portalType: String = DEFAULT_PORTAL_TYPE,
        idempotencyKey: String = Uuid.random().toString(),
    ): Flow<ScrapingProgress> = portalRepository.linkPortal(
        portalType = portalType,
        username = username,
        password = password,
        idempotencyKey = idempotencyKey,
    )

    companion object {
        const val DEFAULT_PORTAL_TYPE = "suwon"
    }
}
