package com.chukchukhaksa.mobile.presentation.timetable.timetablelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.delete_timetable_dialog_body
import chukchukhaksa.composeapp.generated.resources.delete_timetable_dialog_title
import chukchukhaksa.composeapp.generated.resources.timetable_list_screen_empty_timetable
import chukchukhaksa.composeapp.generated.resources.word_cancel
import chukchukhaksa.composeapp.generated.resources.word_confirm
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.CchAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.container.CchEditContainer
import com.chukchukhaksa.mobile.common.designsystem.component.dialog.CchDialog
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimetableListRoute(
    viewModel: TimetableListViewModel = koinViewModel(),
    popBackStack: () -> Unit,
    navigateTimetableEditor: (TimetableEditorArgument) -> Unit,
    navigateSemesterSelect: () -> Unit,
    handleException: (Throwable) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is TimetableListSideEffect.HandleException -> handleException(sideEffect.throwable)
            is TimetableListSideEffect.NavigateTimetableEditor -> navigateTimetableEditor(sideEffect.argument)
            TimetableListSideEffect.NavigateSemesterSelect -> navigateSemesterSelect()
            TimetableListSideEffect.PopBackStack -> popBackStack()
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.initData()
    }

    TimetableListScreen(
        uiState = uiState,
        onClickBack = viewModel::popBackStack,
        onClickAddTextButton = viewModel::navigateSemesterSelect,
        onClickTimetableEditButton = viewModel::navigateTimetableEditor,
        onClickTimetableDeleteButton = viewModel::showDeleteDialog,
        onDismissDeleteDialogRequest = viewModel::hideDeleteDialog,
        onClickDeleteDialogConfirm = viewModel::deleteTimetable,
        onClickDeleteDialogDismiss = viewModel::hideDeleteDialog,
        onClickTimetableContainer = viewModel::setMainTimetable,
    )
}

@Composable
fun TimetableListScreen(
    uiState: TimetableListState = TimetableListState(),
    onClickBack: () -> Unit = {},
    onClickAddTextButton: () -> Unit = {},
    onClickTimetableEditButton: (Timetable) -> Unit = {},
    onClickTimetableDeleteButton: (Timetable) -> Unit = {},
    onDismissDeleteDialogRequest: () -> Unit = {},
    onClickDeleteDialogConfirm: () -> Unit = {},
    onClickDeleteDialogDismiss: () -> Unit = {},
    onClickTimetableContainer: (Long) -> Unit = {},
) {
    SuwikiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White100),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CchAppBarWithTitle(
              title = "시간표 목록",
              isShowAddButton = true,
              onClickBackButton = { onClickBack() },
              onClickAdd = onClickAddTextButton,
            )

            if (uiState.timetableList.isEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(top = 150.dp),
                    textAlign = TextAlign.Center,
                    text = stringResource(Res.string.timetable_list_screen_empty_timetable),
                    style = SuwikiTheme.typography.header4,
                    color = Gray95,
                )
            }

            LazyColumn(
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = uiState.timetableList, key = { it.createTime }) { timetable ->
                    CchEditContainer(
                        name = timetable.name,
                        semester = "${timetable.year}-${timetable.semester}",
                        onClickEditButton = { onClickTimetableEditButton(timetable) },
                        onClickDeleteButton = { onClickTimetableDeleteButton(timetable) },
                        onClick = { onClickTimetableContainer(timetable.createTime) },
                    )
                }
            }
        }

        if (uiState.showDeleteDialog) {
            CchDialog(
                headerText = stringResource(Res.string.delete_timetable_dialog_title),
                bodyText = stringResource(Res.string.delete_timetable_dialog_body),
                confirmButtonText = stringResource(Res.string.word_confirm),
                dismissButtonText = stringResource(Res.string.word_cancel),
                onDismissRequest = onDismissDeleteDialogRequest,
                onClickDismiss = onClickDeleteDialogDismiss,
                onClickConfirm = onClickDeleteDialogConfirm,
            )
        }
    }
}

//@Preview
//@Composable
//fun TimetableListScreenPreview() {
//    SuwikiTheme {
//        TimetableListScreen()
//    }
//}
