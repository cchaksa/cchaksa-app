package com.chukchukhaksa.mobile.common.model.portallink

/** 포털 연동이 실패한 이유. 화면은 종류에 따라 입력 폼으로 돌아가거나 실패 화면을 보여 준다. */
sealed interface PortalLinkFailure {
  /** 분석 이벤트와 로그에 남기는 실패 코드. 서버 오류 코드나 기기 스크래퍼 오류 코드다. */
  val code: String

  /** 학번·비밀번호가 틀렸거나 계정이 잠겼다. 입력을 고쳐 다시 시도한다. */
  data class Credential(override val code: String) : PortalLinkFailure

  /** 입력은 맞지만 학적·데이터 문제로 진행할 수 없다(서버 오류 코드 S04·D01·T13·G02). */
  data class Ineligible(override val code: String) : PortalLinkFailure

  /**
   * 기기 스크래핑은 끝났지만 서버에 저장됐는지 확인하지 못했다(5xx, 네트워크 오류, 응답 유실).
   * 다시 시도할 때는 스크래핑 없이 [upload]를 같은 키로 다시 올린다.
   * 결과에 개인 정보가 들어 있어 toString에 드러나지 않도록 data class로 두지 않는다.
   */
  class UploadFailed(val upload: PortalScrapeUpload) : PortalLinkFailure {
    override val code: String = UPLOAD_FAILED_CODE
  }

  /** 서버 job이 제한 시간 안에 끝나지 않았다. */
  data object Timeout : PortalLinkFailure {
    override val code: String = "TIMEOUT"
  }

  /** 그 밖의 실패. */
  data class System(override val code: String) : PortalLinkFailure

  companion object {
    const val UPLOAD_FAILED_CODE = "UPLOAD_FAILED"
    const val UNKNOWN_CODE = "UNKNOWN"
  }
}

class PortalLinkException(
  val failure: PortalLinkFailure,
  cause: Throwable? = null,
) : Exception("포털 연동 실패: ${failure.code}", cause)
