package com.chukchukhaksa.mobile.common.kmp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AndroidAppLifecycleObserver(application: Application) : AppLifecycleObserver {
  private val _onForeground = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  override val onForeground: SharedFlow<Unit> = _onForeground.asSharedFlow()

  private var startedCount = 0

  init {
    application.registerActivityLifecycleCallbacks(
      object : Application.ActivityLifecycleCallbacks {
        override fun onActivityStarted(activity: Activity) {
          if (startedCount == 0) {
            Napier.d(tag = "AppLifecycle") { "Foreground enter" }
            _onForeground.tryEmit(Unit)
          }
          startedCount++
        }

        override fun onActivityStopped(activity: Activity) {
          startedCount = (startedCount - 1).coerceAtLeast(0)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
      },
    )
  }
}
