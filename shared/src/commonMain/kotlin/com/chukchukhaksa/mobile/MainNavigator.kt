package com.chukchukhaksa.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.chukchukhaksa.mobile.presentation.landing.navigation.LandingRoute
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
        navController.navigateWebView(url)
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
