package com.chukchukhaksa.mobile.common.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.zeroValue
import platform.CoreGraphics.CGRect
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.UIKit.statusBarManager
import androidx.compose.runtime.SideEffect

actual fun getPlatform(): Platform = Platform.IOS

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun statusBarView() = remember {
  val keyWindow: UIWindow? =
    UIApplication.sharedApplication.windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
  val tag = 3848245L

  keyWindow?.viewWithTag(tag) ?: run {
    val height =
      keyWindow?.windowScene?.statusBarManager?.statusBarFrame ?: zeroValue<CGRect>()
    val statusBarView = UIView(frame = height)
    statusBarView.tag = tag
    statusBarView.layer.zPosition = 999999.0
    keyWindow?.addSubview(statusBarView)
    statusBarView
  }
}


@Composable
actual fun SetStatusBarColor() {
  val statusBar = statusBarView()
  SideEffect {
    statusBar.backgroundColor = Color(0xFFFFFFFF).toUIColor()
  }
}

private fun Color.toUIColor(): UIColor = UIColor(
  red = this.red.toDouble(),
  green = this.green.toDouble(),
  blue = this.blue.toDouble(),
  alpha = this.alpha.toDouble()
)
