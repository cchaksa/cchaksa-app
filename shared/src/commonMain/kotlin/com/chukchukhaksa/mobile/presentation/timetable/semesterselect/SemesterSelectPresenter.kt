package com.chukchukhaksa.mobile.presentation.timetable.semesterselect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.SemesterSelectScreen.Event
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.SemesterSelectScreen.State
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputScreen
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter

class SemesterSelectPresenter(
  private val navigator: Navigator,
) : Presenter<State> {

  @Composable
  override fun present(): State {
    // 다음 화면에 갔다가 돌아와도 선택이 남도록, NavHost가 백스택 엔트리별로 저장·복원해 주는 rememberSaveable에 둔다.
    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    return State(selectedSemesterIndex = selectedIndex) { event ->
      when (event) {
        is Event.ClickSemester -> {
          selectedIndex = if (selectedIndex == event.index) null else event.index
        }

        Event.ClickNext -> selectedIndex?.let(semesterList::getOrNull)?.let { semester ->
          navigator.goTo(TimetableNameInputScreen(semester.toTimetableEditorArgument()))
        }

        Event.ClickBack -> navigator.pop()
      }
    }
  }
}
