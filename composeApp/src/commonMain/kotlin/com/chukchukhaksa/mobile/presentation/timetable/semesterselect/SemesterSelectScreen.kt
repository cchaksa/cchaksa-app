package com.chukchukhaksa.mobile.presentation.timetable.semesterselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.ChukChukAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.button.ChukChukBasicButton
import com.chukchukhaksa.mobile.common.designsystem.component.container.ChukChukSelectionContainer
import com.chukchukhaksa.mobile.common.designsystem.theme.CCHaksaTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayFB
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SemesterSelectRoute(
  viewModel: SemesterSelectViewModel = koinViewModel(),
  popBackStack: () -> Unit = {},
  navigateTimetableEditor: (TimetableEditorArgument) -> Unit,
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
  viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
    when (sideEffect) {
      is SemesterSelectSideEffect.NavigateTimetableEditor -> navigateTimetableEditor(sideEffect.semester)
    }
  }
  SemesterSelectScreen(
    uiState = uiState,
    onClickBackButton = popBackStack,
    onClickSemester = viewModel::updateSelectedSemesterIndex,
    onClickNextButton = viewModel::navigateTimetableEditor,
  )
}

@Composable
fun SemesterSelectScreen(
  uiState: SemesterSelectState = SemesterSelectState(),
  onClickBackButton: () -> Unit = {},
  onClickSemester: (Int) -> Unit = {},
  onClickNextButton: (Semester) -> Unit = {},
) {
  val semesters = semesterList.map { it.toText() }.toPersistentList()
  val scrollState = rememberScrollState()

  SuwikiBackground(
    contentWindowInsets = WindowInsets.navigationBars,
    color = GrayFB,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ChukChukAppBarWithTitle(
          modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
          title = "시간표 생성하기",
          onClickBackButton = { onClickBackButton() },
        )

        Text(
          modifier = Modifier.padding(top = 20.dp, bottom = 32.dp),
          text = "수강학기를 선택해주세요",
          textAlign = TextAlign.Center,
          style = CCHaksaTheme.typography.titleLg,
        )

        Column(
          modifier = Modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          semesters.forEachIndexed { idx, semester ->
            ChukChukSelectionContainer(
              modifier = Modifier.fillMaxWidth(),
              text = semester,
              isSelected = idx == uiState.selectSemesterIndex,
              onClick = { onClickSemester(idx) },
            )
          }
        }
      }
      ChukChukBasicButton(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        text = "다음",
        enable = uiState.nextBtnEnable,
        onClick = { onClickNextButton(semesterList[uiState.selectSemesterIndex!!]) },
      )
    }
  }
}
