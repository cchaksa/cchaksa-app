package com.chukchukhaksa.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.chukchukhaksa.mobile.common.extension.record
import com.chukchukhaksa.mobile.common.kmp.isDebug
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputScreen
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.navigation.NavStackList
import com.slack.circuit.runtime.navigation.navStackListOf
import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen

/**
 * NavHost 목적지 안에서 띄운 Circuit 화면의 내비게이션 요청을 기존 [MainNavigator] 호출로 옮기는 어댑터.
 *
 * 화면 키 → NavHost 목적지 매핑은 이 클래스에만 둔다. 매핑이 없는 요청은 디버그 빌드에서 예외로 바로
 * 드러내고, 릴리스 빌드에서는 Crashlytics에 기록한 뒤 실패(false/null/빈 목록)로 돌려준다.
 * NavHost가 백스택을 들고 있으므로 이 navigator는 현재 화면 하나만 안다.
 */
class MainCircuitNavigator(
  private val mainNavigator: MainNavigator,
  private val currentScreen: Screen,
) : Navigator {

  override fun goTo(screen: Screen): Boolean = when (screen) {
    is TimetableNameInputScreen -> {
      mainNavigator.navigateTimetableNameInput(screen.argument)
      true
    }
    else -> unmapped("goTo($screen)")
  }

  override fun pop(result: PopResult?): Screen? {
    // NavHost 목적지 사이에는 결과를 돌려줄 통로가 없다.
    if (result != null) {
      unmapped("pop(result = $result)")
      return null
    }
    mainNavigator.popBackStackIfNotHome()
    return null
  }

  override fun resetRoot(newRoot: Screen, options: Navigator.StateOptions): List<Screen> {
    when (newRoot) {
      LegacyScreen.Home -> mainNavigator.navigateTimetable()
      else -> unmapped("resetRoot($newRoot)")
    }
    return emptyList()
  }

  override fun forward(): Boolean = unmapped("forward()")

  override fun backward(): Boolean = unmapped("backward()")

  override fun peek(): Screen = currentScreen

  override fun peekBackStack(): List<Screen> = listOf(currentScreen)

  override fun peekNavStack(): NavStackList<Screen> = navStackListOf(currentScreen)

  private fun unmapped(request: String): Boolean {
    val error = IllegalStateException("No NavHost mapping for $request from $currentScreen")
    if (isDebug) throw error
    error.record()
    return false
  }
}

/**
 * 목적지가 다시 구성되어도 같은 인스턴스를 쓰도록 [MainCircuitNavigator]를 remember한다.
 * Circuit은 navigator를 키로 presenter를 remember하므로, 인스턴스가 바뀌면 presenter가 다시 만들어진다.
 */
@Composable
fun rememberMainCircuitNavigator(
  mainNavigator: MainNavigator,
  screen: Screen,
): Navigator = remember(mainNavigator, screen) {
  MainCircuitNavigator(mainNavigator, screen)
}
