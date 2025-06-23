package com.chukchukhaksa.mobile.common.kmp

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController

actual fun getPlatform(): Platform = Platform.Android

@Composable
actual fun SetStatusBarColor(){
  val sysUiController = rememberSystemUiController()

  SideEffect {
    sysUiController.setSystemBarsColor(color = Color(0xFFfbfbfb))
  }
}
