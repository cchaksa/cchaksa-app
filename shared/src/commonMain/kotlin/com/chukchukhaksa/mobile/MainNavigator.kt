package com.chukchukhaksa.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.chukchukhaksa.mobile.presentation.debug.navigation.DebugRoute
import com.chukchukhaksa.mobile.presentation.debug.navigation.navigateDebug
import com.chukchukhaksa.mobile.presentation.landing.navigation.LandingRoute
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkMode
import com.chukchukhaksa.mobile.presentation.portallink.navigation.navigatePortalLink
import com.chukchukhaksa.mobile.presentation.timetable.navigation.HomeRoute
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateCellEditor
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateOpenLecture
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateSemesterSelect
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateTimetable
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateTimetableEditor
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateTimetableNameInput
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateTimetableList
import com.chukchukhaksa.mobile.presentation.webview.navigation.navigateWebView

class MainNavigator(
    val navController: NavHostController,
) {
    val startDestination = HomeRoute.route


    fun navigateCellEditor(argument: CellEditorArgument) {
        navController.navigateCellEditor(argument)
    }

    fun navigateTimetableNameInput(argument: TimetableEditorArgument = TimetableEditorArgument()) {
        navController.navigateTimetableNameInput(argument)
    }

    fun navigateTimetableEditor(argument: TimetableEditorArgument = TimetableEditorArgument()) {
      navController.navigateTimetableEditor(argument)
    }

    fun navigateTimetableList() {
        navController.navigateTimetableList()
    }

    fun navigateSemesterSelect() {
      navController.navigateSemesterSelect()
    }

    fun navigateOpenLecture() {
        navController.navigateOpenLecture()
    }

    fun navigateTimetable() {
        navController.navigateTimetable()
    }

    fun navigateWebView(url: String) {
        // 포털 연동·학업 정보 업데이트 웹 화면은 네이티브로 옮겼다. 웹 홈의 이동 요청과 광고 게이트를 거친 이동이
        // 모두 여기로 오므로 이 자리에서 네이티브 화면으로 돌린다.
        val portalLinkMode = PortalLinkMode.fromWebUrl(url)
        if (portalLinkMode != null) {
            navController.navigatePortalLink(portalLinkMode)
        } else {
            navController.navigateWebView(url)
        }
    }

    fun navigatePortalLink(mode: PortalLinkMode) {
        navController.navigatePortalLink(mode)
    }

    fun navigateDebug() {
        // 디버그 화면에서 다시 흔들어도 화면을 겹쳐 쌓지 않는다.
        if (isSameCurrentDestination(DebugRoute.route)) return
        navController.navigateDebug()
    }

    fun navigateToHome() {
        // 이미 홈(HomeRoute)에 있으면 탭만 갱신하면 되므로 네비게이션은 하지 않는다.
        if (isSameCurrentDestination(HomeRoute.route)) return

        // 홈이 백스택에 있으면(홈 → 웹뷰 진입) 홈까지 pop, 없으면(랜딩 → 웹뷰 진입) 백스택을 비우고 홈으로 이동.
        val poppedToHome = navController.popBackStack(HomeRoute.route, inclusive = false)
        if (!poppedToHome) {
            navController.navigate(HomeRoute.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun navigateFromLandingToHome() {
        navController.navigate(HomeRoute.route) {
            popUpTo(LandingRoute.route) {
                inclusive = true
            }
        }
    }

    fun navigateToLanding() {
        navController.navigate(LandingRoute.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun popBackStackIfNotHome() {
        if (!isSameCurrentDestination(HomeRoute.route)) {
            navController.popBackStack()
        }
    }

    private fun isSameCurrentDestination(route: String) =
        navController.currentDestination?.route == route

}

@Composable
fun rememberMainNavigator(
    navController: NavHostController = rememberNavController(),
): MainNavigator = remember(navController) {
    MainNavigator(navController)
}
