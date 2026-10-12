package com.chukchukhaksa.mobile.common.kmp

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification

class IosAppLifecycleObserver : AppLifecycleObserver {
  private val _onForeground = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  override val onForeground: SharedFlow<Unit> = _onForeground.asSharedFlow()

  init {
    NSNotificationCenter.defaultCenter.addObserverForName(
      name = UIApplicationDidBecomeActiveNotification,
      `object` = null,
      queue = NSOperationQueue.mainQueue,
    ) { _ ->
      Napier.d(tag = "AppLifecycle") { "Foreground enter" }
      _onForeground.tryEmit(Unit)
    }
  }
}
