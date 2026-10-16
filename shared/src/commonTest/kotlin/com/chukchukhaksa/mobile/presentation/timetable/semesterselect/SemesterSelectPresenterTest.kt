package com.chukchukhaksa.mobile.presentation.timetable.semesterselect

import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.SemesterSelectScreen.Event
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputScreen
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SemesterSelectPresenterTest {

    private val navigator = FakeNavigator(SemesterSelectScreen)
    private val presenter = SemesterSelectPresenter(navigator)

    @Test
    fun initiallyNoSemesterIsSelectedAndNextIsDisabled() = runTest {
        presenter.test {
            val state = awaitItem()
            assertNull(state.selectedSemesterIndex)
            assertFalse(state.nextButtonEnabled)
        }
    }

    @Test
    fun selectingSemesterEnablesNextAndSelectingItAgainClearsIt() = runTest {
        presenter.test {
            awaitItem().eventSink(Event.ClickSemester(1))

            val selected = awaitItem()
            assertEquals(1, selected.selectedSemesterIndex)
            assertTrue(selected.nextButtonEnabled)

            selected.eventSink(Event.ClickSemester(1))

            val cleared = awaitItem()
            assertNull(cleared.selectedSemesterIndex)
            assertFalse(cleared.nextButtonEnabled)
        }
    }

    @Test
    fun selectingAnotherSemesterMovesSelection() = runTest {
        presenter.test {
            awaitItem().eventSink(Event.ClickSemester(1))
            awaitItem().eventSink(Event.ClickSemester(3))

            assertEquals(3, awaitItem().selectedSemesterIndex)
        }
    }

    @Test
    fun nextGoesToNameInputWithSelectedSemester() = runTest {
        presenter.test {
            awaitItem().eventSink(Event.ClickSemester(2))
            awaitItem().eventSink(Event.ClickNext)

            assertEquals(
                TimetableNameInputScreen(semesterList[2].toTimetableEditorArgument()),
                navigator.awaitNextScreen(),
            )
        }
    }

    @Test
    fun nextWithoutSelectionDoesNotNavigate() = runTest {
        presenter.test {
            awaitItem().eventSink(Event.ClickNext)

            navigator.expectNoGoToEvents()
        }
    }

    @Test
    fun backPops() = runTest {
        presenter.test {
            awaitItem().eventSink(Event.ClickBack)

            navigator.awaitPop()
        }
    }
}
