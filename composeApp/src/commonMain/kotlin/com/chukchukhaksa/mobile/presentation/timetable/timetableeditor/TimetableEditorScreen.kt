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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.create_timetable_screen_placeholder
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.CchAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.CchSelectBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.component.container.CchSelectionButton
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.CchRegularTextField
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.semesterList
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimetableEditorRoute(
  viewModel: TimetableEditorViewModel = koinViewModel(),
  popBackStack: () -> Unit,
  handleException: (Throwable) -> Unit,
  onShowToast: (String, Dp) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is TimetableEditorSideEffect.HandleException -> handleException(sideEffect.throwable)
            TimetableEditorSideEffect.PopBackStack -> popBackStack()
            TimetableEditorSideEffect.ShowEditSaveToast -> onShowToast("변경사항이 저장되었습니다.", 111.dp)
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
            CchAppBarWithTitle(
              title = "${uiState.semester?.year}년 ${uiState.semester?.semester}학기",
              isShowAddButton = false,
              onClickBackButton = { onClickBack() },
            )

            Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            ) {
                CchSelectionButton(
                  modifier = Modifier.padding(horizontal = 4.dp),
                  title = uiState.semester?.toText() ?: "수강학기 선택",
                  onClick = onClickSelectionContainer,
                )

                CchRegularTextField(
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
                    value = uiState.name,
                    onValueChanged = onValueChangeTimetableName,
                    placeholder = stringResource(Res.string.create_timetable_screen_placeholder),
                )

                Spacer(modifier = Modifier.weight(1f))

                CchBasicButton(
                    modifier = Modifier
                        .consumeWindowInsets(WindowInsets.navigationBars)
                        .imePadding(),
                    text = "변경사항 저장하기",
                    enable = uiState.buttonEnabled,
                    onClick = onClickCompleteButton,
                )
            }
        }
    }
    if (uiState.isSheetOpenSemester) {
        CchSelectBottomSheet(
          onDismissRequest = hideSemesterBottomSheet,
          onClickItem = { onClickSemesterItem(it) },
          itemList = semesterList.map { it.toText() }.toPersistentList(),
          selectedPosition = uiState.selectedSemesterPosition,
        )
    }
}

//@Preview
//@Composable
//fun TimetableEditorScreenPreview() {
//    SuwikiTheme {
//        TimetableEditorScreen()
//    }
//}
