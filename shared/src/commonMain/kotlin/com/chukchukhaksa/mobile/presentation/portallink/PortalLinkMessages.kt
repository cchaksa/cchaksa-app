package com.chukchukhaksa.mobile.presentation.portallink

import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkFailure

// 웹 features/portal-link/utils/errorMapping.ts의 문구를 그대로 옮겼다.
private val MESSAGES_BY_CODE = mapOf(
  "S04" to "이미 포털 연동된 학생 정보가 존재합니다.\n다른 계정으로 로그인했는지 확인해주세요.",
  "D01" to "복수전공 이수 구분 정보가 존재하지 않아 처리할 수 없습니다.\n학사정보를 확인해주세요.",
  "T13" to "편입생 학적 정보는 현재 지원되지 않습니다.\n추후 지원 예정입니다.",
  "G02" to "사용자에게 맞는 졸업 요건 데이터가 존재하지 않습니다.\n학과/입학년도 정보를 확인해주세요.",
  "PORTAL_AUTH_FAILED" to "아이디나 비밀번호가 일치하지 않습니다.\n학교 홈페이지에서 확인해주세요.",
  "PORTAL_LOGIN_FAILED" to "아이디나 비밀번호가 일치하지 않습니다.\n학교 홈페이지에서 확인해주세요.",
  "PORTAL_ACCOUNT_LOCKED" to "계정이 잠겼습니다. 포털사이트로 돌아가서 학번/사번 찾기 및 비밀번호 재발급을 진행해주세요",
  // 웹에는 없는 코드다. 앱 업로드 API가 학번이 다른 계정에 연결됐거나 이미 연결된 학번과 다를 때 보낸다.
  "C21" to "현재 계정에 연결할 수 없는 학번입니다.\n다른 계정으로 로그인했는지 확인해주세요.",
)

private const val DEFAULT_MESSAGE = "알 수 없는 오류가 발생했어요\n잠시후 다시 시도해주세요"

private const val TIMEOUT_MESSAGE = "연동 응답이 3분 이상 없어 자동으로 실패 처리되었어요.\n잠시 후 다시 시도해주세요."

// 웹에는 없는 경우다. 기기에서 불러온 정보를 서버에 저장하지 못했을 때 보여 준다.
private const val UPLOAD_FAILED_MESSAGE = "불러온 학업 정보를 저장하지 못했어요\n잠시 후 다시 시도해주세요"

internal fun PortalLinkFailure.toMessage(): String = when (this) {
  is PortalLinkFailure.Timeout -> TIMEOUT_MESSAGE
  is PortalLinkFailure.UploadFailed -> UPLOAD_FAILED_MESSAGE
  else -> MESSAGES_BY_CODE[code] ?: DEFAULT_MESSAGE
}
