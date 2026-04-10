package com.chukchukhaksa.mobile.remote.portal

import com.chukchukhaksa.mobile.domain.portal.model.PortalScrapingError

internal fun mapToPortalScrapingError(
    httpStatus: Int?,
    appCode: String?,
): PortalScrapingError = when {
    appCode == "S04" -> PortalScrapingError.AlreadyConnected
    appCode == "D01" -> PortalScrapingError.DoubleMajorInfoMissing
    appCode == "T13" -> PortalScrapingError.TransferStudentNotSupported
    appCode == "G02" -> PortalScrapingError.GraduationDataNotFound
    httpStatus == 401 -> PortalScrapingError.InvalidCredentials
    httpStatus == 404 -> PortalScrapingError.GraduationDataNotFound
    httpStatus == 409 -> PortalScrapingError.AlreadyConnected
    httpStatus == 422 -> PortalScrapingError.InvalidAcademicRecord
    httpStatus == 423 -> PortalScrapingError.AccountLocked
    else -> PortalScrapingError.Unknown(httpStatus = httpStatus, appCode = appCode)
}
