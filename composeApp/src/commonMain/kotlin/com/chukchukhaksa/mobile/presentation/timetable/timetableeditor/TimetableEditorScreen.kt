package com.chukchukhaksa.mobile.presentation.timetable.timetableeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.create_timetable_need_select_semester
import chukchukhaksa.composeapp.generated.resources.create_timetable_screen_placeholder
import chukchukhaksa.composeapp.generated.resources.word_complete
import chukchukhaksa.composeapp.generated.resources.word_select_semester
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.SuwikiAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.SuwikiSelectBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedLargeButton
import com.chukchukhaksa.mobile.common.designsystem.component.container.SuwikiSelectionContainer
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.SuwikiRegularTextField
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimetableEditorRoute(
    viewModel: TimetableEditorViewModel = koinViewModel(),
    popBackStack: () -> Unit,
    handleException: (Throwable) -> Unit,
    onShowToast: (String) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is TimetableEditorSideEffect.HandleException -> handleException(sideEffect.throwable)
            TimetableEditorSideEffect.PopBackStack -> popBackStack()
            TimetableEditorSideEffect.NeedSelectSemesterToast -> onShowToast(
                getString(Res.string.create_timetable_need_select_semester),
            )
        }
    }
    TimetableEditorScreen(
        uiState = uiState,
        onValueChangeTimetableName = viewModel::updateName,
        onClickBack = viewModel::popBackStack,
        onClickCompleteButton = viewModel::upsertTimetable,
        onClickSelectionContainer = viewModel::showSemesterBottomSheet,
        hideSemesterBottomSheet = viewModel::hideSemesterBottomSheet,
        onClickSemesterItem = { position ->
            viewModel.hideSemesterBottomSheet()
            viewModel.updateSemesterPosition(position)
        },
        onClickTextFieldClearButton = { viewModel.updateName("") },
    )
}

@Composable
fun TimetableEditorScreen(
    uiState: TimetableEditorState = TimetableEditorState(),
    onValueChangeTimetableName: (String) -> Unit = {},
    onClickTextFieldClearButton: () -> Unit = {},
    onClickBack: () -> Unit = {},
    onClickCompleteButton: () -> Unit = {},
    onClickSelectionContainer: () -> Unit = {},
    hideSemesterBottomSheet: () -> Unit = {},
    onClickSemesterItem: (Int) -> Unit = {},
) {
    SuwikiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
        ) {
            SuwikiAppBarWithTitle(
                showCloseIcon = false,
                onClickBack = onClickBack,
            )

            Spacer(modifier = Modifier.size(20.dp))

            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                SuwikiSelectionContainer(
                    title = uiState.semester?.toText() ?: stringResource(Res.string.word_select_semester),
                    onClick = onClickSelectionContainer,
                )

                Spacer(modifier = Modifier.size(22.dp))

                SuwikiRegularTextField(
                    value = uiState.name,
                    onValueChange = onValueChangeTimetableName,
                    onClickClearButton = onClickTextFieldClearButton,
                    placeholder = stringResource(Res.string.create_timetable_screen_placeholder),
                )

                Spacer(modifier = Modifier.weight(1f))

                SuwikiContainedLargeButton(
                    modifier = Modifier
                        .consumeWindowInsets(WindowInsets.navigationBars)
                        .imePadding(),
                    text = stringResource(Res.string.word_complete),
                    enabled = uiState.buttonEnabled,
                    clickable = uiState.buttonEnabled,
                    onClick = onClickCompleteButton,
                )
            }
        }
    }


    SuwikiSelectBottomSheet(
        isSheetOpen = uiState.isSheetOpenSemester,
        onDismissRequest = hideSemesterBottomSheet,
        onClickItem = { onClickSemesterItem(it) },
        itemList = semesterList.map { it.toText() }.toPersistentList(),
        title = stringResource(Res.string.word_select_semester),
        selectedPosition = uiState.selectedSemesterPosition,
    )
}

//@Preview
//@Composable
//fun TimetableEditorScreenPreview() {
//    SuwikiTheme {
//        TimetableEditorScreen()
//    }
//}
