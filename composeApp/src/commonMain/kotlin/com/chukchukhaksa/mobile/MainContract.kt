package com.chukchukhaksa.mobile

data class MainState(
  val toastMessage: String = "",
  val toastVisible: Boolean = false,
  val showNetworkErrorDialog: Boolean = false,
  val showForceUpdateDialog: Boolean = false,
)

sealed interface MainSideEffect {
  data class OpenUrl(val url: String) : MainSideEffect
}
