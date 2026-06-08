package com.chukchukhaksa.mobile.presentation.webview

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 웹뷰 브릿지의 redirectToHome 액션처럼 "앱 홈으로 이동" 요청을 앱 전역에 전달하는 이벤트 버스.
 * - App: 홈(HomeRoute)으로 네비게이션한다.
 * - HomeViewModel: 포털 연동 여부에 따라 홈/시간표 탭을 선택한다.
 */
class HomeRedirectEventBus {
  private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val events: SharedFlow<Unit> = _events.asSharedFlow()

  fun redirectToHome() {
    _events.tryEmit(Unit)
  }
}
