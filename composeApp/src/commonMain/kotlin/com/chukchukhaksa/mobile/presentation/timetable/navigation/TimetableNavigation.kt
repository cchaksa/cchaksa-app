package com.chukchukhaksa.mobile.presentation.timetable.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chukchukhaksa.mobile.common.ui.encodeToUri
import com.chukchukhaksa.mobile.presentation.timetable.celleditor.CellEditorRoute
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.openlecture.OpenLectureRoute
import com.chukchukhaksa.mobile.presentation.timetable.timetable.HomeRoute
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputRoute
import com.chukchukhaksa.mobile.presentation.timetable.timetablelist.TimetableListRoute
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.SemesterSelectRoute
import com.chukchukhaksa.mobile.presentation.timetable.timetableeditor.TimetableEditorRoute
import kotlinx.serialization.json.Json

fun NavController.navigateTimetableNameInput(argument: TimetableEditorArgument = TimetableEditorArgument()) {
    navigate(TimetableRoute.timetableNameInputRoute(Json.encodeToUri(argument)))
}

fun NavController.navigateTimetableEditor(argument: TimetableEditorArgument = TimetableEditorArgument()) {
  navigate(TimetableRoute.timetableEditorRoute(Json.encodeToUri(argument)))
}

fun NavController.navigateTimetableList() {
    navigate(TimetableRoute.timetableListRoute)
}

fun NavController.navigateOpenLecture() {
    navigate(TimetableRoute.openLectureRoute)
}

fun NavController.navigateCellEditor(argument: CellEditorArgument = CellEditorArgument()) {
    navigate(TimetableRoute.cellEditorRoute(Json.encodeToUri(argument)))
}

fun NavController.navigateSemesterSelect() {
    navigate(TimetableRoute.semesterSelectRoute)
}

fun NavController.navigateTimetable() {
    navigate(HomeRoute.route) {
      popUpTo(0)
    }
}

fun NavGraphBuilder.timetableNavGraph(
    padding: PaddingValues,
    popBackStack: () -> Unit,
    navigateTimetableNameInput: (TimetableEditorArgument) -> Unit,
    navigateTimetableEditor: (TimetableEditorArgument) -> Unit,
    navigateTimetableList: () -> Unit,
    navigateOpenLecture: () -> Unit,
    navigateCellEditor: (CellEditorArgument) -> Unit,
    navigateSemesterSelect: () -> Unit,
    navigateTimetable:() -> Unit,
    navigateWebView: (String) -> Unit,
    navigateToLanding: () -> Unit,
    handleException: (Throwable) -> Unit,
    onShowToast: (String, Dp) -> Unit,
) {
    composable(route = HomeRoute.route) {
        HomeRoute(
            padding = padding,
            navigateTimetableList = navigateTimetableList,
            handleException = handleException,
            onShowToast = onShowToast,
            navigateOpenLecture = navigateOpenLecture,
            navigateCellEditor = navigateCellEditor,
            navigateSemesterSelect = navigateSemesterSelect,
            navigateWebView = navigateWebView,
            navigateToLanding = navigateToLanding,
        )
    }

    composable(
        route = TimetableRoute.timetableNameInputRoute(
            "{${TimetableRoute.TIMETABLE_EDITOR_ARGUMENT}}",
        ),
        arguments = listOf(
            navArgument(TimetableRoute.TIMETABLE_EDITOR_ARGUMENT) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        TimetableNameInputRoute(
            navigateTimetable = navigateTimetable,
            popBackStack = popBackStack,
            handleException = handleException,
            onShowToast = onShowToast,
        )
    }

    composable(
      route = TimetableRoute.timetableEditorRoute(
        "{${TimetableRoute.TIMETABLE_EDITOR_ARGUMENT}}",
      ),
      arguments = listOf(
        navArgument(TimetableRoute.TIMETABLE_EDITOR_ARGUMENT) {
          type = NavType.StringType
          nullable = true
        },
      ),
    ) {
      TimetableEditorRoute(
        popBackStack = popBackStack,
        handleException = handleException,
        onShowToast = onShowToast,
      )
    }

    composable(route = TimetableRoute.openLectureRoute) {
        OpenLectureRoute(
            selectedOpenMajor = null,
            popBackStack = popBackStack,
            handleException = handleException,
            onShowToast = onShowToast,
            navigateCellEditor = navigateCellEditor,
        )
    }

    composable(
        route = TimetableRoute.cellEditorRoute("{${TimetableRoute.CELL_EDITOR_ARGUMENT}}"),
        arguments = listOf(
            navArgument(TimetableRoute.CELL_EDITOR_ARGUMENT) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        CellEditorRoute(
            popBackStack = popBackStack,
            handleException = handleException,
            onShowToast = onShowToast,
        )
    }

    composable(
        route = TimetableRoute.timetableListRoute,
    ) {
        TimetableListRoute(
            handleException = handleException,
            popBackStack = popBackStack,
            navigateTimetableEditor = navigateTimetableEditor,
            navigateSemesterSelect = navigateSemesterSelect,
            onShowToast = onShowToast,
        )
    }

  composable(
    route = TimetableRoute.semesterSelectRoute,
  ) {
      SemesterSelectRoute(
        popBackStack = popBackStack,
        navigateTimetableNameInput = navigateTimetableNameInput,
    )
  }
}

/** 앱의 메인 탭 호스트(홈/시간표/마이페이지) = 시작 목적지. */
object HomeRoute {
    const val route = "home"
}

/** 시간표 탭에서 진입하는 하위 화면들의 라우트. */
object TimetableRoute {
    private const val base = "timetable"
    const val openLectureRoute = "open-lecture"
    const val timetableListRoute = "$base/list"
    const val semesterSelectRoute = "semester-select"
    const val CELL_EDITOR_ARGUMENT = "cell-editor-argument"
    const val TIMETABLE_EDITOR_ARGUMENT = "timetable-editor-argument"

    fun timetableNameInputRoute(timetableEditor: String) = "$base/name-input/$timetableEditor"
    fun timetableEditorRoute(timetableEditor: String) = "$base/editor/$timetableEditor"
    fun cellEditorRoute(cellEditor: String) = "cell-editor/$cellEditor"
}
