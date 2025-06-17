package com.chukchukhaksa.mobile.presentation.timetable.timetableeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.decodeFromUri
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.timetable.usecase.InsertTimetableUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.UpdateTimetableUseCase
import com.chukchukhaksa.mobile.presentation.timetable.navigation.TimetableRoute
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class TimetableEditorViewModel(
    private val insertTimetableUseCase: InsertTimetableUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val argument = savedStateHandle.get<String>(TimetableRoute.TIMETABLE_EDITOR_ARGUMENT)!!
    private val timetableEditorArgument = Json.decodeFromUri<TimetableEditorArgument>(argument)
    private val isEditMode = timetableEditorArgument.isEditMode
    val mviStore: MviStore<TimetableEditorState, TimetableEditorSideEffect> = mviStore(
        timetableEditorArgument.toState(),
    )

    fun showSemesterBottomSheet() {
        mviStore.setState { copy(isSheetOpenSemester = true) }
    }

    fun hideSemesterBottomSheet() {
        mviStore.setState { copy(isSheetOpenSemester = false) }
    }

    fun updateSemesterPosition(position: Int) {
        mviStore.setState { copy(selectedSemesterPosition = position) }
    }

    fun updateName(name: String) {
        mviStore.setState { copy(name = name) }
    }

    fun upsertTimetable() = viewModelScope.launch {
        val state = mviStore.uiState.value

        if (state.semester == null) {
            mviStore.postSideEffect(TimetableEditorSideEffect.NeedSelectSemesterToast)
            return@launch
        }

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
                mviStore.postSideEffect(TimetableEditorSideEffect.PopBackStack)
            }.onFailure {
                mviStore.postSideEffect(TimetableEditorSideEffect.HandleException(it))
            }
    }

    fun popBackStack() {
        mviStore.postSideEffect(TimetableEditorSideEffect.PopBackStack)
    }
}
