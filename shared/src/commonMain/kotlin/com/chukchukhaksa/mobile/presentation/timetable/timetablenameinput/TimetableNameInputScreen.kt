package com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput

import com.chukchukhaksa.mobile.common.extension.checkTimetableNameRule
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.Semester
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.serialization.Serializable

/**
 * 시간표 이름을 정하고 시간표를 만드는(또는 이름·학기를 고치는) 화면.
 * [TimetableEditorArgument.isEditMode]이면 기존 시간표를 고치고, 아니면 새로 만든다.
 */
@Serializable
data class TimetableNameInputScreen(
  val argument: TimetableEditorArgument,
) : Screen {
  data class State(
    val name: String,
    val semester: Semester,
    val eventSink: (Event) -> Unit,
  ) : CircuitUiState {
    val buttonEnabled: Boolean get() = checkTimetableNameRule(name)
  }

  sealed interface Event : CircuitUiEvent {
    data class ChangeName(val name: String) : Event
    data object ClickBack : Event
    data object ClickComplete : Event
  }
}
