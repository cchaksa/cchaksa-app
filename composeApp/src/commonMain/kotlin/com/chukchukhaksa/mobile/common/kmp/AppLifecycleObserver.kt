package com.chukchukhaksa.mobile.common.kmp

import kotlinx.coroutines.flow.SharedFlow

interface AppLifecycleObserver {
  val onForeground: SharedFlow<Unit>
}
