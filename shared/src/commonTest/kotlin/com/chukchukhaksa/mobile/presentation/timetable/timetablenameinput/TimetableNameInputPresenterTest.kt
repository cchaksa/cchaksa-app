package com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput

import com.chukchukhaksa.mobile.LegacyScreen
import com.chukchukhaksa.mobile.common.ui.ExceptionHandler
import com.chukchukhaksa.mobile.data.timetable.repository.FakeTimetableRepository
import com.chukchukhaksa.mobile.data.timetable.repository.FakeTimetableRepository.Upsert
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.Semester
import com.chukchukhaksa.mobile.presentation.timetable.timetable.ShowTimetableTabEventBus
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputScreen.Event
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TimetableNameInputPresenterTest {

    private val repository = FakeTimetableRepository()
    private val showTimetableTabEventBus = ShowTimetableTabEventBus()
    private val exceptionHandler = RecordingExceptionHandler()

    private fun presenter(argument: TimetableEditorArgument): Pair<TimetableNameInputPresenter, FakeNavigator> {
        val screen = TimetableNameInputScreen(argument)
        val navigator = FakeNavigator(screen)
        return TimetableNameInputPresenter(
            screen = screen,
            navigator = navigator,
            timetableRepository = repository,
            showTimetableTabEventBus = showTimetableTabEventBus,
            exceptionHandler = exceptionHandler,
        ) to navigator
    }

    /** [ShowTimetableTabEventBus]는 구독자가 없으면 요청을 버리므로, presenter를 돌리기 전에 구독해 둔다. */
    private fun TestScope.collectTabRequests(): List<Unit> {
        val requests = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            showTimetableTabEventBus.events.collect { requests += it }
        }
        return requests
    }

    @Test
    fun initialStateComesFromArgument() = runTest {
        val (presenter, _) = presenter(TimetableEditorArgument(name = "1학기", year = "2026", semester = "1"))

        presenter.test {
            val state = awaitItem()
            assertEquals("1학기", state.name)
            assertEquals(Semester("2026", "1"), state.semester)
            assertTrue(state.buttonEnabled)
        }
    }

    @Test
    fun buttonFollowsTimetableNameRule() = runTest {
        val (presenter, _) = presenter(TimetableEditorArgument(year = "2026", semester = "1"))

        presenter.test {
            val empty = awaitItem()
            assertFalse(empty.buttonEnabled)

            empty.eventSink(Event.ChangeName("   "))
            val blank = awaitItem()
            assertFalse(blank.buttonEnabled)

            blank.eventSink(Event.ChangeName("a".repeat(21)))
            val tooLong = awaitItem()
            assertFalse(tooLong.buttonEnabled)

            tooLong.eventSink(Event.ChangeName("a".repeat(20)))
            assertTrue(awaitItem().buttonEnabled)
        }
    }

    @Test
    fun completeInsertsNewTimetableThenShowsTimetableTabAtHome() = runTest {
        val tabRequests = collectTabRequests()
        val (presenter, navigator) = presenter(TimetableEditorArgument(year = "2026", semester = "2"))

        presenter.test {
            awaitItem().eventSink(Event.ChangeName("새 시간표"))
            awaitItem().eventSink(Event.ClickComplete)

            assertEquals(LegacyScreen.Home, navigator.awaitResetRoot().newRoot)
        }
        assertEquals(listOf(Upsert(null, "2026", "2", "새 시간표")), repository.inserted)
        assertEquals(emptyList(), repository.updated)
        assertEquals(1, tabRequests.size)
    }

    @Test
    fun completeInEditModeUpdatesTimetable() = runTest {
        val (presenter, navigator) = presenter(
            TimetableEditorArgument(createTime = 42L, name = "기존", year = "2025", semester = "1"),
        )

        presenter.test {
            awaitItem().eventSink(Event.ChangeName("바꾼 이름"))
            awaitItem().eventSink(Event.ClickComplete)

            assertEquals(LegacyScreen.Home, navigator.awaitResetRoot().newRoot)
        }
        assertEquals(listOf(Upsert(42L, "2025", "1", "바꾼 이름")), repository.updated)
        assertEquals(emptyList(), repository.inserted)
    }

    @Test
    fun failureIsHandedToExceptionHandlerWithoutNavigating() = runTest {
        val tabRequests = collectTabRequests()
        val error = IllegalStateException("db")
        repository.failure = error
        val (presenter, navigator) = presenter(TimetableEditorArgument(name = "이름", year = "2026", semester = "1"))

        presenter.test {
            awaitItem().eventSink(Event.ClickComplete)
        }
        assertSame(error, exceptionHandler.handled.single())
        navigator.expectNoResetRootEvents()
        assertEquals(0, tabRequests.size)
    }

    @Test
    fun backPops() = runTest {
        val (presenter, navigator) = presenter(TimetableEditorArgument(year = "2026", semester = "1"))

        presenter.test {
            awaitItem().eventSink(Event.ClickBack)

            navigator.awaitPop()
        }
    }

    private class RecordingExceptionHandler : ExceptionHandler {
        val handled = mutableListOf<Throwable>()

        override fun handleException(throwable: Throwable) {
            handled += throwable
        }
    }
}
