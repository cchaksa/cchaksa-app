package com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.chukchukhaksa.mobile.LegacyScreen
import com.chukchukhaksa.mobile.common.extension.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.common.ui.ExceptionHandler
import com.chukchukhaksa.mobile.data.timetable.repository.TimetableRepository
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.Semester
import com.chukchukhaksa.mobile.presentation.timetable.timetable.ShowTimetableTabEventBus
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputScreen.Event
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputScreen.State
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.launch

class TimetableNameInputPresenter(
  private val screen: TimetableNameInputScreen,
  private val navigator: Navigator,
  private val timetableRepository: TimetableRepository,
  private val showTimetableTabEventBus: ShowTimetableTabEventBus,
  private val exceptionHandler: ExceptionHandler,
) : Presenter<State> {

  @Composable
  override fun present(): State {
    val argument = screen.argument
    // 입력 중인 이름도 학기 선택과 같은 기준으로, 백스택 엔트리가 살아 있는 동안 rememberSaveable로 보존한다.
    var name by rememberSaveable { mutableStateOf(argument.name) }
    val scope = rememberCoroutineScope()

    return State(
      name = name,
      semester = Semester(argument.year, argument.semester),
    ) { event ->
      when (event) {
        is Event.ChangeName -> name = event.name
        Event.ClickBack -> navigator.pop()
        Event.ClickComplete -> scope.launch { upsertTimetable(name) }
      }
    }
  }

  private suspend fun upsertTimetable(name: String) {
    val argument = screen.argument
    runCatchingIgnoreCancelled {
      if (argument.isEditMode) {
        timetableRepository.updateTimetable(
          createTime = argument.createTime,
          year = argument.year,
          semester = argument.semester,
          name = name,
        )
      } else {
        timetableRepository.insertTimetable(
          year = argument.year,
          semester = argument.semester,
          name = name,
        )
      }
    }.onSuccess {
      // 홈 엔트리를 보존한 채 시간표 탭으로 전환 + 최신 시간표 재조회하도록 살아있는 HomeViewModel에 알린다.
      showTimetableTabEventBus.request()
      navigator.resetRoot(LegacyScreen.Home)
    }.onFailure(exceptionHandler::handleException)
  }
}
