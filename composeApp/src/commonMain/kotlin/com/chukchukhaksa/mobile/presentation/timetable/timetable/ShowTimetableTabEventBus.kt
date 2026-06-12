package com.chukchukhaksa.mobile.presentation.timetable.timetable

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 시간표 생성·수정 완료 후, 홈(HomeRoute) 엔트리를 재생성하지 않고 보존한 채
 * 살아있는 [HomeViewModel]에 "시간표 탭으로 전환하고 메인 시간표를 다시 불러오라"고 알리는 이벤트 버스.
 *
 * 홈을 새로 띄우면(navigate + popUpTo) HomeViewModel/WebViewGuideViewModel이 함께 재생성되어
 * 홈 웹뷰의 렌더 완료 상태(isContentRendered)가 초기화되고, 이미 로드된 웹뷰는 rendered 이벤트를
 * 다시 보내지 않아 홈 탭에 들어갈 때마다 스켈레톤이 다시 뜬다. 이를 막기 위해 엔트리를 보존하고
 * 탭 전환만 신호로 전달한다.
 */
class ShowTimetableTabEventBus {
  private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val events: SharedFlow<Unit> = _events.asSharedFlow()

  fun request() {
    _events.tryEmit(Unit)
  }
}
