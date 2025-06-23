package com.chukchukhaksa.mobile.presentation.timetable.timetable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.timetable_screen_need_create_timetable
import chukchukhaksa.composeapp.generated.resources.timetable_screen_select_type_cell_title
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.SuwikiSelectBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayFB
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.EditTimetableCellBottomSheet
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.TimetableAppbar
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.TimetableEmptyColumn
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.Timetable
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimetableRoute(
    padding: PaddingValues,
    viewModel: TimetableViewModel = koinViewModel(),
    navigateTimetableEditor: () -> Unit,
    navigateOpenLecture: () -> Unit,
    navigateTimetableList: () -> Unit,
    handleException: (Throwable) -> Unit,
    onShowToast: (String) -> Unit,
    navigateCellEditor: (CellEditorArgument) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is TimetableSideEffect.HandleException -> handleException(sideEffect.throwable)
            TimetableSideEffect.NavigateAddTimetableCell -> navigateOpenLecture()
            TimetableSideEffect.ShowNeedCreateTimetableToast -> onShowToast(getString(Res.string.timetable_screen_need_create_timetable))
            TimetableSideEffect.NavigateTimetableEditor -> navigateTimetableEditor()
            is TimetableSideEffect.NavigateCellEditor -> navigateCellEditor(sideEffect.argument)
            TimetableSideEffect.NavigateTimetableList -> navigateTimetableList()
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.getMainTimetable()
    }

    TimetableScreen(
        padding = padding,
        uiState = uiState,
        onClickAddTimetable = viewModel::navigateTimetableEditor,
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
) {
    SuwikiBackground(
        contentWindowInsets = WindowInsets.navigationBars,
        color = GrayFB
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TimetableAppbar(
                modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
                name = uiState.timetable?.name,
                onClickAdd = onClickAppbarAdd,
                onClickHamburger = onClickHamburger,
                onClickSetting = onClickSetting,
            )

            AnimatedVisibility(
                visible = uiState.showTimetableEmptyColumn == true,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                TimetableEmptyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(White),
                    onClickAdd = onClickAddTimetable,
                )
            }

            AnimatedVisibility(
                visible = uiState.showTimetableEmptyColumn == false,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Timetable(
                    timetable = uiState.timetable ?: com.chukchukhaksa.mobile.common.model.Timetable(),
                    type = uiState.cellType,
                    onClickTimetableCell = onClickTimetableCell,
                )
            }
        }
    }

    SuwikiSelectBottomSheet(
        isSheetOpen = uiState.showSelectCellTypeBottomSheet,
        onDismissRequest = onDismissSelectBottomSheet,
        onClickItem = onClickSelectBottomSheetItem,
        itemList = TimetableCellType.entries.map { it.text }
            .toPersistentList(),
        title = stringResource(Res.string.timetable_screen_select_type_cell_title),
        selectedPosition = uiState.cellType.ordinal,
    )

    EditTimetableCellBottomSheet(
        isSheetOpen = uiState.showEditCellBottomSheet,
        onDismissRequest = onDismissEditCellBottomSheet,
        cell = uiState.selectedCell,
        onClickDeleteButton = onClickTimetableCellDeleteButton,
        onClickEditButton = onClickTimetableCellEditButton,
    )
}

//@Preview
//@Composable
//fun TimetableScreenPreview() {
//    SuwikiTheme {
//        TimetableScreen(padding = PaddingValues(0.dp))
//    }
//}
