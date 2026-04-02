package com.chukchukhaksa.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.chukchukhaksa.mobile.presentation.landing.navigation.LandingRoute
import com.chukchukhaksa.mobile.presentation.timetable.navigation.TimetableRoute
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateCellEditor
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateGraduationProgress
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateOpenLecture
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateSemesterSelect
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateTimetable
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateTimetableEditor
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateTimetableNameInput
import com.chukchukhaksa.mobile.presentation.timetable.navigation.navigateTimetableList

class MainNavigator(
    val navController: NavHostController,
) {
    val startDestination = TimetableRoute.route


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

    fun navigateGraduationProgress() {
        navController.navigateGraduationProgress()
    }

    fun navigateTimetable() {
        navController.navigateTimetable()
    }

    fun navigateFromLandingToHome() {
        navController.navigate(TimetableRoute.route) {
            popUpTo(LandingRoute.route) {
                inclusive = true
            }
        }
    }

    fun popBackStackIfNotHome() {
        if (!isSameCurrentDestination(TimetableRoute.route)) {
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
