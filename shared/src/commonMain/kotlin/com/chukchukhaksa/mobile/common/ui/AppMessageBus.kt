package com.chukchukhaksa.mobile.common.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

val DefaultToastBottomPadding: Dp = 70.dp

/** 앱 전역 토스트(App의 CchToast)를 띄운다. 연속 요청은 앞의 토스트가 사라진 뒤 순서대로 보인다. */
interface ToastNotifier {
  fun showToast(message: String, bottomPadding: Dp = DefaultToastBottomPadding)
}

/** 화면에서 처리하지 못한 예외를 앱 전역 규칙(네트워크 오류 다이얼로그 또는 안내 토스트 + 기록)으로 처리한다. */
interface ExceptionHandler {
  fun handleException(throwable: Throwable)
}

/**
 * [ToastNotifier]와 [ExceptionHandler] 요청을 앱 전역에 전달하는 이벤트 버스.
 * 화면은 인터페이스만 주입받아 호출하고, MainViewModel이 [events]를 구독해 토스트 상태와
 * 네트워크 오류 다이얼로그를 갱신한다.
 */
class AppMessageBus : ToastNotifier, ExceptionHandler {
  // 한 프레임 안에 여러 요청이 몰려도 구독자가 꺼내 가기 전에 버려지지 않도록 버퍼를 넉넉히 둔다.
  private val _events = MutableSharedFlow<AppMessageEvent>(extraBufferCapacity = 64)
  val events: SharedFlow<AppMessageEvent> = _events.asSharedFlow()

  override fun showToast(message: String, bottomPadding: Dp) {
    _events.tryEmit(AppMessageEvent.ShowToast(message, bottomPadding))
  }

  override fun handleException(throwable: Throwable) {
    _events.tryEmit(AppMessageEvent.HandleException(throwable))
  }
}

sealed interface AppMessageEvent {
  data class ShowToast(val message: String, val bottomPadding: Dp) : AppMessageEvent
  data class HandleException(val throwable: Throwable) : AppMessageEvent
}
