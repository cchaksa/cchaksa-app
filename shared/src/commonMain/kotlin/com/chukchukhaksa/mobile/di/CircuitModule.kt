package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.SemesterSelectPresenter
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.SemesterSelectScreen
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.SemesterSelectUi
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputPresenter
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputScreen
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputUi
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

/**
 * Circuit으로 전환한 화면의 presenter와 [Circuit] 인스턴스.
 * 화면을 전환할 때는 presenter를 여기에 factory로 등록하고, [KoinPresenterFactory]와 [AppUiFactory]에 키를 추가한다.
 */
val circuitModule = module {
  factory { (_: SemesterSelectScreen, navigator: Navigator) ->
    SemesterSelectPresenter(navigator = navigator)
  }
  factory { (screen: TimetableNameInputScreen, navigator: Navigator) ->
    TimetableNameInputPresenter(
      screen = screen,
      navigator = navigator,
      timetableRepository = get(),
      showTimetableTabEventBus = get(),
      exceptionHandler = get(),
    )
  }

  single {
    Circuit.Builder()
      .addPresenterFactory(KoinPresenterFactory(getKoin()))
      .addUiFactory(AppUiFactory)
      .build()
  }
}

/** 화면 키에 맞는 presenter를 Koin에서 `parametersOf(screen, navigator)`로 만든다. */
private class KoinPresenterFactory(private val koin: Koin) : Presenter.Factory {
  override fun create(screen: Screen, navigator: Navigator, context: CircuitContext): Presenter<*>? =
    when (screen) {
      SemesterSelectScreen -> koin.get<SemesterSelectPresenter> { parametersOf(screen, navigator) }
      is TimetableNameInputScreen -> koin.get<TimetableNameInputPresenter> { parametersOf(screen, navigator) }
      else -> null
    }
}

private object AppUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    SemesterSelectScreen -> ui<SemesterSelectScreen.State> { state, modifier ->
      SemesterSelectUi(state = state, modifier = modifier)
    }
    is TimetableNameInputScreen -> ui<TimetableNameInputScreen.State> { state, modifier ->
      TimetableNameInputUi(state = state, modifier = modifier)
    }
    else -> null
  }
}
