package com.chukchukhaksa.mobile.presentation.timetable.timetable

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 시간표 생성·수정 완료 후, 홈(HomeRoute) 엔트리를 재생성하지 않고 보존한 채
 * 살아있는 [HomeViewModel]에 "시간표 탭으로 전환하고 메인 시간표를 다시 불러오라"고 알리는 이벤트 버스.
 *
 * 홈을 새로 띄우면(navigate + popUpTo) HomeViewModel/WebViewGuideViewModel이 함께 재생성되어
 * 홈 화면 상태를 처음부터 다시 만든다. 웹뷰 렌더 완료 여부는 WebViewHolder가 기억하므로 스켈레톤이
 * 다시 뜨지는 않지만, 굳이 재생성할 이유가 없어 엔트리를 보존하고 탭 전환만 신호로 전달한다.
 */
class ShowTimetableTabEventBus {
  private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val events: SharedFlow<Unit> = _events.asSharedFlow()

  fun request() {
    _events.tryEmit(Unit)
  }
}
