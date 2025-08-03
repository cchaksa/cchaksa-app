package com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.create_timetable_need_select_semester
import chukchukhaksa.composeapp.generated.resources.create_timetable_screen_placeholder
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.CchAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.CchRegularTextField
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimetableNameInputRoute(
  viewModel: TimetableNameInputViewModel = koinViewModel(),
  navigateTimetable: () -> Unit,
  popBackStack: () -> Unit,
  handleException: (Throwable) -> Unit,
  onShowToast: (String, Dp) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is TimetableNameInputSideEffect.HandleException -> handleException(sideEffect.throwable)
            TimetableNameInputSideEffect.PopBackStack -> popBackStack()
            TimetableNameInputSideEffect.NeedSelectSemesterToast -> onShowToast(
                getString(Res.string.create_timetable_need_select_semester),
                70.dp,
            )
            TimetableNameInputSideEffect.NavigateTimetable -> navigateTimetable()
        }
    }
    TimetableNameInputScreen(
        uiState = uiState,
        onValueChangeTimetableName = viewModel::updateName,
        onClickBack = viewModel::popBackStack,
        onClickCompleteButton = viewModel::upsertTimetable,
        onClickTextFieldClearButton = { viewModel.updateName("") },
    )
}

@Composable
fun TimetableNameInputScreen(
  uiState: TimetableNameInputState = TimetableNameInputState(),
  onValueChangeTimetableName: (String) -> Unit = {},
  onClickTextFieldClearButton: () -> Unit = {},
  onClickBack: () -> Unit = {},
  onClickCompleteButton: () -> Unit = {},
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
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                  modifier = Modifier
                    .width(240.dp)
                    .padding(top = 8.dp),
                  text = "선택한 학기의 시간표 이름을 정해주세요",
                  style = CchTheme.typography.titleLg,
                  color = Black100,
                  textAlign = TextAlign.Center,
                )

                CchRegularTextField(
                    modifier = Modifier.padding(top = 196.dp, start = 4.dp, end = 4.dp),
                    value = uiState.name,
                    placeholder = stringResource(Res.string.create_timetable_screen_placeholder),
                    isActive = uiState.name.isNotEmpty(),
                    onValueChanged = onValueChangeTimetableName,
                    onClickClearButton = onClickTextFieldClearButton,
                )

                Spacer(modifier = Modifier.weight(1f))

                CchBasicButton(
                    modifier = Modifier
                        .consumeWindowInsets(WindowInsets.navigationBars),
                    text = "시간표 생성하기",
                    enable = uiState.buttonEnabled,
                    onClick = onClickCompleteButton,
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun TimetableEditorScreenPreview() {
//    SuwikiTheme {
//        TimetableEditorScreen()
//    }
//}
