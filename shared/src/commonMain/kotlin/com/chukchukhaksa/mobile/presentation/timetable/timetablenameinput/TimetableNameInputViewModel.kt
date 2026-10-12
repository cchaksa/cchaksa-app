package com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.decodeFromUri
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.timetable.usecase.InsertTimetableUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.UpdateTimetableUseCase
import com.chukchukhaksa.mobile.presentation.timetable.navigation.TimetableRoute
import com.chukchukhaksa.mobile.presentation.timetable.timetable.ShowTimetableTabEventBus
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class TimetableNameInputViewModel(
    private val insertTimetableUseCase: InsertTimetableUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val showTimetableTabEventBus: ShowTimetableTabEventBus,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val argument = savedStateHandle.get<String>(TimetableRoute.TIMETABLE_EDITOR_ARGUMENT)!!
    private val timetableEditorArgument = Json.decodeFromUri<TimetableEditorArgument>(argument)
    private val isEditMode = timetableEditorArgument.isEditMode
    val mviStore: MviStore<TimetableNameInputState, TimetableNameInputSideEffect> = mviStore(
        timetableEditorArgument.toState(),
    )

    fun updateName(name: String) {
        mviStore.setState { copy(name = name) }
    }

    fun upsertTimetable() = viewModelScope.launch {
        val state = mviStore.uiState.value

        val useCase = if (isEditMode) {
            updateTimetableUseCase(
                param = UpdateTimetableUseCase.Param(
                    createTime = timetableEditorArgument.createTime,
                    name = state.name,
                    year = state.semester.year,
                    semester = state.semester.semester,
                ),
            )
        } else {
            insertTimetableUseCase(
                param = InsertTimetableUseCase.Param(
                    name = state.name,
                    year = state.semester.year,
                    semester = state.semester.semester,
                ),
            )
        }

        useCase
            .onSuccess {
                // 홈 엔트리를 보존한 채 시간표 탭으로 전환 + 최신 시간표 재조회하도록 살아있는 HomeViewModel에 알린다.
                showTimetableTabEventBus.request()
                mviStore.postSideEffect(TimetableNameInputSideEffect.NavigateTimetable)
            }.onFailure {
                mviStore.postSideEffect(TimetableNameInputSideEffect.HandleException(it))
            }
    }

    fun popBackStack() {
        mviStore.postSideEffect(TimetableNameInputSideEffect.PopBackStack)
    }
}
