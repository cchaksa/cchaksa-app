package com.chukchukhaksa.mobile.domain.portal.model

sealed class PortalScrapingError(val defaultMessage: String) {

    data object InvalidCredentials : PortalScrapingError(
        "아이디나 비밀번호가 일치하지 않습니다.\n학교 홈페이지에서 확인해주세요.",
    )

    data object AccountLocked : PortalScrapingError(
        "계정이 잠겼습니다. 포털사이트에서 비밀번호 재설정을 진행해주세요.",
    )

    data object InvalidAcademicRecord : PortalScrapingError(
        "입력하신 학적 정보로는 현재 처리가 불가능합니다.\n세부 사유를 확인해주세요.",
    )

    data object AlreadyConnected : PortalScrapingError(
        "이미 포털 연동된 학생 정보가 존재합니다.\n다른 계정으로 로그인했는지 확인해주세요.",
    )

    data object GraduationDataNotFound : PortalScrapingError(
        "사용자에게 맞는 졸업 요건 데이터가 존재하지 않습니다.\n학과/입학년도 정보를 확인해주세요.",
    )

    data object DoubleMajorInfoMissing : PortalScrapingError(
        "복수전공 이수 구분 정보가 존재하지 않아 처리할 수 없습니다.\n학사정보를 확인해주세요.",
    )

    data object TransferStudentNotSupported : PortalScrapingError(
        "편입생 학적 정보는 현재 지원되지 않습니다.\n추후 지원 예정입니다.",
    )

    data class Unknown(
        val httpStatus: Int?,
        val appCode: String?,
    ) : PortalScrapingError(
        "알 수 없는 오류가 발생했어요.\n잠시 후 다시 시도해주세요.",
    )
}
