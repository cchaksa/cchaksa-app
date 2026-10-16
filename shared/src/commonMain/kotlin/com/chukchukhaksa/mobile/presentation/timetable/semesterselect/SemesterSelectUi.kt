package com.chukchukhaksa.mobile.presentation.timetable.semesterselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.CchAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.component.container.CchSelectionContainer
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.SemesterSelectScreen.Event
import kotlinx.collections.immutable.toPersistentList

@Composable
fun SemesterSelectUi(
  state: SemesterSelectScreen.State,
  modifier: Modifier = Modifier,
) {
  val semesters = semesterList.map { it.toText() }.toPersistentList()
  val scrollState = rememberScrollState()

  SuwikiBackground(modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CchAppBarWithTitle(
          title = "시간표 생성하기",
          isShowAddButton = false,
          onClickBackButton = { state.eventSink(Event.ClickBack) },
        )

        Text(
          modifier = Modifier.padding(top = 20.dp, bottom = 32.dp),
          text = "수강학기를 선택해주세요",
          textAlign = TextAlign.Center,
          style = CchTheme.typography.titleLg,
        )

        Column(
          modifier = Modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          semesters.forEachIndexed { idx, semester ->
            CchSelectionContainer(
              modifier = Modifier.fillMaxWidth(),
              text = semester,
              isSelected = idx == state.selectedSemesterIndex,
              onClick = { state.eventSink(Event.ClickSemester(idx)) },
            )
          }

          Spacer(Modifier.height(120.dp))
        }
      }
      CchBasicButton(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(start = 16.dp, end = 16.dp, bottom = 36.dp),
        text = "다음",
        enable = state.nextButtonEnabled,
        onClick = { state.eventSink(Event.ClickNext) },
      )
    }
  }
}
