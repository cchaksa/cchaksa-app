package com.chukchukhaksa.mobile.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue

// iosApp의 UIWindow.motionEnded(ContentView.swift)가 흔들기 동작을 이 이름의 알림으로 보낸다.
// 흔들기는 UIKit 응답 체인으로만 전달되어 Kotlin에서 직접 받을 수 없다.
private const val DEVICE_DID_SHAKE_NOTIFICATION = "ChukChukHaksaDeviceDidShake"

@Composable
actual fun ShakeEffect(onShake: () -> Unit) {
  val currentOnShake by rememberUpdatedState(onShake)

  DisposableEffect(Unit) {
    val observer = NSNotificationCenter.defaultCenter.addObserverForName(
      name = DEVICE_DID_SHAKE_NOTIFICATION,
      `object` = null,
      queue = NSOperationQueue.mainQueue,
    ) { _ -> currentOnShake() }
    onDispose { NSNotificationCenter.defaultCenter.removeObserver(observer) }
  }
}
