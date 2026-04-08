package com.chukchukhaksa.mobile.presentation.home.graduationprogress

import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessListData

data class GraduationProgressState(
  val graduationProgress: GraduationProcessListData = GraduationProcessListData(),
  val isLoading: Boolean = false,
)

sealed interface GraduationProgressSideEffect {
    data class HandleException(val throwable: Throwable) : GraduationProgressSideEffect
}
