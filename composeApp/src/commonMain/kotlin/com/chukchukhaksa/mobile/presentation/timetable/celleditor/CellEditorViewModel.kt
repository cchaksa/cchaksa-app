package com.chukchukhaksa.mobile.presentation.timetable.celleditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableCellOverlapException
import com.chukchukhaksa.mobile.common.model.TimetableCellPeriodInvalidException
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.decodeFromUri
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.timetable.usecase.InsertTimetableCellUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.UpdateTimetableCellUseCase
import com.chukchukhaksa.mobile.presentation.timetable.navigation.TimetableRoute
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CellEditorViewModel(
    private val insertTimetableCellUseCase: InsertTimetableCellUseCase,
    private val updateTimetableCellUseCase: UpdateTimetableCellUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val argument = savedStateHandle.get<String>(TimetableRoute.CELL_EDITOR_ARGUMENT)!!
    private val cellEditorArgument = Json.decodeFromUri<CellEditorArgument>(argument)
    private val isEditMode = cellEditorArgument.isEditMode

    val mviStore: MviStore<CellEditorState, CellEditorSideEffect> = mviStore(
        cellEditorArgument.toState(),
    )

    fun updateCellColor(color: TimetableCellColor) {
        mviStore.setState { copy(selectedTimetableCellColor = color) }
    }

    fun addCell() {
        mviStore.setState {
            copy(
                cellStateList = cellStateList.add(CellState()),
            )
        }
    }

    fun deleteCell(index: Int) {
        mviStore.setState {
            copy(
                cellStateList = cellStateList.removeAt(index),
            )
        }
    }

    fun updateCellLocation(index: Int, location: String) {
        val toUpdateCell = mviStore.uiState.value.cellStateList[index].copy(location = location)
        reduceCell(index, toUpdateCell)
    }

    fun updateCellEndPeriod(index: Int, endPeriod: String) {
        val toUpdateCell =
            mviStore.uiState.value.cellStateList[index].copy(endPeriod = endPeriod.toPeriod())
        reduceCell(index, toUpdateCell)
    }

    fun updateCellStartPeriod(index: Int, startPeriod: String) {
        val toUpdateCell =
            mviStore.uiState.value.cellStateList[index].copy(startPeriod = startPeriod.toPeriod())
        reduceCell(index, toUpdateCell)
    }

    private fun String.toPeriod(): String {
        return toIntOrNull()?.let {
            when {
                it < 1 -> "1"
                it > 15 -> "15"
                else -> this
            }
        } ?: ""
    }

    fun updateCellDay(index: Int, day: TimetableDay) {
        val toUpdateCell = mviStore.uiState.value.cellStateList[index].copy(day = day)
        reduceCell(index, toUpdateCell)
    }

    private fun reduceCell(
        index: Int,
        toUpdateCell: CellState,
    ) {
        val cellList = mviStore.uiState.value.cellStateList.removeAt(index)
        mviStore.setState {
            copy(
                cellStateList = cellList.add(index, toUpdateCell),
            )
        }
    }

    fun updateLectureName(lectureName: String) {
        mviStore.setState {
            copy(lectureName = lectureName)
        }
    }

    fun updateProfessorName(professorName: String) {
        mviStore.setState {
            copy(professorName = professorName)
        }
    }

    fun upsertTimetable() {
        val state = mviStore.uiState.value
        val sideEffect = when {
            state.lectureName.isEmpty() -> CellEditorSideEffect.ShowNeedLectureNameToast
            state.professorName.isEmpty() -> CellEditorSideEffect.ShowNeedProfessorNameToast
            state.cellStateList.any { it.location.isEmpty() } -> CellEditorSideEffect.ShowNeedLocationToast
            else -> null
        }

        sideEffect?.run {
            mviStore.postSideEffect(this)
            return
        }

        val timetableCellList = if (state.cellStateList.isEmpty()) {
            listOf(
                TimetableCell(
                    name = state.lectureName,
                    professor = state.professorName,
                    location = "",
                    day = TimetableDay.E_LEARNING,
                    startPeriod = 0,
                    endPeriod = 0,
                    color = state.selectedTimetableCellColor,
                ),
            )
        } else {
            state.cellStateList.map { cell ->
                TimetableCell(
                    name = state.lectureName,
                    professor = state.professorName,
                    location = cell.location,
                    day = cell.day,
                    startPeriod = cell.startPeriod.toIntOrNull() ?: 0,
                    endPeriod = cell.endPeriod.toIntOrNull() ?: 0,
                    color = state.selectedTimetableCellColor,
                )
            }
        }

        viewModelScope.launch {
            val useCase = if (isEditMode) {
                updateTimetableCellUseCase(
                    UpdateTimetableCellUseCase.Param(
                        cellEditorArgument.oldCellId,
                        timetableCellList,
                    ),
                )
            } else {
                insertTimetableCellUseCase(timetableCellList)
            }

            useCase
                .onSuccess {
                    if (isEditMode) {
                        mviStore.postSideEffect(CellEditorSideEffect.ShowEditSuccessCellToast)
                    } else {
                        mviStore.postSideEffect(CellEditorSideEffect.ShowAddSuccessCellToast)
                    }

                    popBackStack()
                }
                .onFailure { throwable ->
                    when (throwable) {
                        is TimetableCellOverlapException -> mviStore.postSideEffect(
                            CellEditorSideEffect.ShowToast(
                                throwable.message
                            )
                        )

                        is TimetableCellPeriodInvalidException -> mviStore.postSideEffect(
                            CellEditorSideEffect.ShowToast(
                                throwable.message
                            )
                        )

                        else -> mviStore.postSideEffect(CellEditorSideEffect.HandleException(throwable))
                    }
                }
        }
    }

    fun popBackStack() {
        mviStore.postSideEffect(CellEditorSideEffect.PopBackStack)
    }
}
