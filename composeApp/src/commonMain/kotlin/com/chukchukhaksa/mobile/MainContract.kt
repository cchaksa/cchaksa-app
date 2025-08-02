package com.chukchukhaksa.mobile

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class MainState(
  val toastMessage: String = "",
  val toastVisible: Boolean = false,
  val toastBottomPadding: Dp = 70.dp,
  val showNetworkErrorDialog: Boolean = false,
  val showForceUpdateDialog: Boolean = false,
)

sealed interface MainSideEffect {
  data class OpenUrl(val url: String) : MainSideEffect
}
