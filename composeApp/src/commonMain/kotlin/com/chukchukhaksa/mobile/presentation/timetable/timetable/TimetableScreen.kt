package com.chukchukhaksa.mobile.presentation.timetable.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.timetable_screen_need_create_timetable
import chukchukhaksa.composeapp.generated.resources.timetable_screen_select_type_cell_title
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.CchSelectBottomSheet
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.EditTimetableCellBottomSheet
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.TimetableAppbar
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.TimetableEmptyColumn
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.Timetable
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType
import com.chukchukhaksa.mobile.common.designsystem.component.tabbar.TimetableTabBar
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.chukchukhaksa.mobile.widget.sendWidgetUpdateCommand
import com.chukchukhaksa.mobile.common.provider.LocalAppContext
import com.chukchukhaksa.mobile.presentation.home.home.HomeRoute
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.WebViewGuideScreen

@Composable
fun TimetableRoute(
  padding: PaddingValues,
  viewModel: TimetableViewModel = koinViewModel(),
  navigateOpenLecture: () -> Unit,
  navigateTimetableList: () -> Unit,
  handleException: (Throwable) -> Unit,
  onShowToast: (String, Dp) -> Unit,
  navigateCellEditor: (CellEditorArgument) -> Unit,
  navigateSemesterSelect: () -> Unit,
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

  val context = LocalAppContext.current
  viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
    when (sideEffect) {
      is TimetableSideEffect.HandleException -> handleException(sideEffect.throwable)
      TimetableSideEffect.NavigateAddTimetableCell -> navigateOpenLecture()
      TimetableSideEffect.ShowNeedCreateTimetableToast -> onShowToast(getString(Res.string.timetable_screen_need_create_timetable), 70.dp)
      is TimetableSideEffect.NavigateCellEditor -> navigateCellEditor(sideEffect.argument)
      TimetableSideEffect.NavigateTimetableList -> navigateTimetableList()
      TimetableSideEffect.NavigateSemesterSelect -> navigateSemesterSelect()
    }
  }

  LaunchedEffect(key1 = Unit) {
    viewModel.getMainTimetable()
  }

  LaunchedEffect(key1 = uiState.timetable) {
    if (context != null) {
      if (uiState.timetable != null) {
        sendWidgetUpdateCommand(context)
      }
    }
  }

  TimetableScreen(
    padding = padding,
    uiState = uiState,
    onClickAddTimetable = viewModel::navigateSemesterSelect,
    onClickAppbarAdd = viewModel::navigateAddTimetableCell,
    onClickTimetableCell = viewModel::showEditCellBottomSheet,
    onDismissEditCellBottomSheet = viewModel::hideEditCellBottomSheet,
    onClickTimetableCellDeleteButton = viewModel::deleteCell,
    onClickTimetableCellEditButton = { cell ->
      viewModel.hideEditCellBottomSheet()
      viewModel.navigateCellEdit(cell)
    },
    onDismissSelectBottomSheet = viewModel::hideSelectCellTypeBottomSheet,
    onClickSelectBottomSheetItem = { position ->
      viewModel.updateCellType(position)
      viewModel.hideSelectCellTypeBottomSheet()
    },
    onClickSetting = viewModel::showSelectCellTypeBottomSheet,
    onClickHamburger = viewModel::navigateTimetableList,
    onClickHome = viewModel::showHomeScreen,
    onClickTimetable = viewModel::getMainTimetable,
    onClickMyPage = viewModel::showHomeScreen,
  )
}

@Composable
fun TimetableScreen(
  padding: PaddingValues,
  uiState: TimetableState = TimetableState(),
  onClickAddTimetable: () -> Unit = {},
  onClickAppbarAdd: () -> Unit = {},
  onClickTimetableCell: (TimetableCell) -> Unit = {},
  onDismissEditCellBottomSheet: () -> Unit = {},
  onClickTimetableCellDeleteButton: (TimetableCell) -> Unit = {},
  onClickTimetableCellEditButton: (TimetableCell) -> Unit = {},
  onDismissSelectBottomSheet: () -> Unit = {},
  onClickSelectBottomSheetItem: (Int) -> Unit = {},
  onClickSetting: () -> Unit = {},
  onClickHamburger: () -> Unit = {},
  onClickHome: () -> Unit = {},
  onClickTimetable: () -> Unit = {},
  onClickMyPage: () -> Unit = {}
) {
    val semester = "${uiState.timetable?.year}년 ${uiState.timetable?.semester}학기"
    SuwikiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TimetableTabBar(
                timetableScreenContent = uiState.timetableScreen ?: TimetableScreen.EMPTY_TIMETABLE,
                onClickHome = { onClickHome() },
                onClickTimetable = { onClickTimetable() },
                onClickMyPage = { onClickMyPage() }
            )

            if(uiState.timetableScreen == TimetableScreen.EMPTY_TIMETABLE) {
              TimetableEmptyColumn(
                modifier = Modifier
                  .fillMaxSize()
                  .background(White100),
                onClickAdd = onClickAddTimetable,
              )
            }

            if(uiState.timetableScreen == TimetableScreen.TIMETABLE) {
              Column {
                TimetableAppbar(
                  modifier = Modifier.padding(top = 8.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                  semester = semester,
                  name = uiState.timetable?.name,
                  onClickAdd = onClickAppbarAdd,
                  onClickHamburger = onClickHamburger,
                  onClickSetting = onClickSetting,
                )
                Timetable(
                  timetable = uiState.timetable ?: com.chukchukhaksa.mobile.common.model.Timetable(),
                  type = uiState.cellType,
                  onClickTimetableCell = onClickTimetableCell,
                )
              }
            }

            if(uiState.timetableScreen == TimetableScreen.HOME) {
//              WebViewGuideScreen()
              HomeRoute()
            }
        }
    }

  if (uiState.showSelectCellTypeBottomSheet) {
    CchSelectBottomSheet(
      onDismissRequest = onDismissSelectBottomSheet,
      onClickItem = onClickSelectBottomSheetItem,
      itemList = TimetableCellType.entries.map { it.text }
        .toPersistentList(),
      title = stringResource(Res.string.timetable_screen_select_type_cell_title),
      selectedPosition = uiState.cellType.ordinal,
    )
  }


  if (uiState.showEditCellBottomSheet) {
    EditTimetableCellBottomSheet(
      onDismissRequest = onDismissEditCellBottomSheet,
      cell = uiState.selectedCell,
      onClickDeleteButton = onClickTimetableCellDeleteButton,
      onClickEditButton = onClickTimetableCellEditButton,
    )
  }
}

//@Preview
//@Composable
//fun TimetableScreenPreview() {
//    SuwikiTheme {
//        TimetableScreen(padding = PaddingValues(0.dp))
//    }
//}
