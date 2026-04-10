package com.chukchukhaksa.mobile.domain.portal.model

class PortalScrapingException(
    val error: PortalScrapingError,
    val httpStatus: Int?,
    val appCode: String?,
    override val message: String = error.defaultMessage,
) : RuntimeException(message)
