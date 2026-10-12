package com.chukchukhaksa.mobile.presentation.webview

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 웹뷰 브릿지의 redirectToHome 액션처럼 "앱 홈으로 이동" 요청을 앱 전역에 전달하는 이벤트 버스.
 * - App: 홈(HomeRoute)으로 네비게이션하고, 필요 시 홈 웹뷰를 재로드한다.
 * - HomeViewModel: 포털 연동 여부에 따라 홈/시간표 탭을 선택한다.
 */
class HomeRedirectEventBus {
  private val _events = MutableSharedFlow<HomeRedirectEvent>(extraBufferCapacity = 1)
  val events: SharedFlow<HomeRedirectEvent> = _events.asSharedFlow()

  /**
   * 랜딩 → 포털 웹뷰 → 홈 신규 진입처럼 HomeViewModel이 아직 [events]를 구독하기 전에 발생한 이벤트를,
   * 새로 생성된 HomeViewModel이 init 시점에 한 번 받아갈 수 있도록 보관하는 1회성 보류분.
   */
  private var pending: HomeRedirectEvent? = null

  fun redirectToHome(reloadWebView: Boolean = false) {
    val event = HomeRedirectEvent(reloadWebView = reloadWebView)
    pending = event
    _events.tryEmit(event)
  }

  /** 보류 중인 이벤트를 한 번만 꺼내 간다(꺼내면 비워진다). */
  fun consumePending(): HomeRedirectEvent? = pending.also { pending = null }
}

data class HomeRedirectEvent(
  val reloadWebView: Boolean = false,
)
