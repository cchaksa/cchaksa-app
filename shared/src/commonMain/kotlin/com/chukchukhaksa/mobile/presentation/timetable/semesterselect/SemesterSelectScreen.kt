package com.chukchukhaksa.mobile.presentation.timetable.semesterselect

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.serialization.Serializable

/** 새 시간표를 만들 학기를 고르는 화면. 고른 학기를 들고 [com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputScreen]으로 간다. */
@Serializable
data object SemesterSelectScreen : Screen {
  data class State(
    /** [semesterList]에서 고른 학기의 위치. 고르지 않았으면 null. */
    val selectedSemesterIndex: Int?,
    val eventSink: (Event) -> Unit,
  ) : CircuitUiState {
    val nextButtonEnabled: Boolean get() = selectedSemesterIndex != null
  }

  sealed interface Event : CircuitUiEvent {
    /** 이미 고른 학기를 다시 누르면 선택을 해제한다. */
    data class ClickSemester(val index: Int) : Event
    data object ClickNext : Event
    data object ClickBack : Event
  }
}
