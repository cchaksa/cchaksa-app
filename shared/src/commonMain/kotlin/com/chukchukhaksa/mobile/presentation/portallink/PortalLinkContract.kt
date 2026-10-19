package com.chukchukhaksa.mobile.presentation.portallink

import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkMode

data class PortalLinkState(
  val mode: PortalLinkMode,
  val step: PortalLinkStep = PortalLinkStep.FORM,
  val studentId: String = "",
  val password: String = "",
  /** 입력 폼 아래에 보여 주는 오류. 학번·비밀번호 오류처럼 입력을 고쳐서 해결하는 실패에 쓴다. */
  val formErrorMessage: String? = null,
  val failureMessage: String = "",
  val isUnavailableDialogVisible: Boolean = false,
) {
  val canSubmit: Boolean
    get() = step == PortalLinkStep.FORM && studentId.isNotBlank() && password.isNotBlank()
}

enum class PortalLinkStep {
  FORM,
  LOADING,
  FAILURE,
}

sealed interface PortalLinkSideEffect {
  data object PopBackStack : PortalLinkSideEffect
}
