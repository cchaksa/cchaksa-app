package com.chukchukhaksa.mobile

import com.slack.circuit.runtime.screen.Screen
import kotlinx.serialization.Serializable

/**
 * 아직 Circuit으로 전환하지 않은 NavHost 목적지를 가리키는 화면 키.
 *
 * presenter가 전환되지 않은 화면으로 이동해야 할 때 이 키를 [com.slack.circuit.runtime.Navigator]에 넘기면,
 * [MainCircuitNavigator]가 기존 [MainNavigator] 호출로 바꾼다. Circuit 팩토리는 이 키를 다루지 않는다.
 * 해당 목적지를 Circuit으로 전환하면 이 키를 지우고 그 화면의 `XxxScreen` 키로 바꾼다.
 */
sealed interface LegacyScreen : Screen {
  /**
   * 앱의 메인 탭 호스트(HomeRoute). `resetRoot`로 보내면 홈 엔트리를 보존한 채 홈까지 pop한다.
   * 홈을 새로 만들지 않으므로, 탭 전환이 필요하면 이동 전에 ShowTimetableTabEventBus로 따로 알린다.
   */
  @Serializable
  data object Home : LegacyScreen
}
