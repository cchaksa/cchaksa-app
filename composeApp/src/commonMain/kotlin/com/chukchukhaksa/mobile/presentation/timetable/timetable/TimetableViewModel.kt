package com.chukchukhaksa.mobile.presentation.timetable.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.timetable.usecase.DeleteTimetableCellUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetMainTimetableUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetTimetableCellTypeUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.SetTimetableCellTypeUseCase
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.toCellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType
import kotlinx.coroutines.launch

class TimetableViewModel(
    private val getMainTimetableUseCase: GetMainTimetableUseCase,
    private val getTimetableCellTypeUseCase: GetTimetableCellTypeUseCase,
    private val deleteTimetableCellUseCase: DeleteTimetableCellUseCase,
    private val setTimetableCellTypeUseCase: SetTimetableCellTypeUseCase,
) : ViewModel() {
    val mviStore = mviStore<TimetableState, TimetableSideEffect>(TimetableState())

    fun getMainTimetable() = viewModelScope.launch {
        val cellType = TimetableCellType.getType(getTimetableCellTypeUseCase().getOrNull())

        getMainTimetableUseCase()
            .onSuccess { timetable ->
                mviStore.setState {
                    copy(
                        timetable = timetable,
                        cellType = cellType,
                        showTimetableEmptyColumn = timetable == null,
                    )
                }
            }
            .onFailure {
                mviStore.postSideEffect(TimetableSideEffect.HandleException(it))
            }
    }

    fun deleteCell(cell: TimetableCell) = viewModelScope.launch {
        deleteTimetableCellUseCase(cell)
            .onSuccess {
                mviStore.setState {
                    copy(
                        showEditCellBottomSheet = false,
                        timetable = it,
                    )
                }
            }
            .onFailure { mviStore.postSideEffect(TimetableSideEffect.HandleException(it)) }
    }

    fun showEditCellBottomSheet(cell: TimetableCell) = viewModelScope.launch {
        mviStore.setState {
            copy(
                showEditCellBottomSheet = true,
                selectedCell = cell,
            )
        }
    }

    fun updateCellType(position: Int) = viewModelScope.launch {
        val cellType = TimetableCellType.entries[position]
        setTimetableCellTypeUseCase(cellType.name)
            .onSuccess { mviStore.setState { copy(cellType = cellType) } }
    }

    fun showSelectCellTypeBottomSheet() {
        mviStore.setState { copy(showSelectCellTypeBottomSheet = true) }
    }

    fun hideSelectCellTypeBottomSheet() {
        mviStore.setState { copy(showSelectCellTypeBottomSheet = false) }
    }

    fun navigateCellEdit(cell: TimetableCell) {
        mviStore.postSideEffect(TimetableSideEffect.NavigateCellEditor(cell.toCellEditorArgument()))
    }

    fun hideEditCellBottomSheet() {
        mviStore.setState { copy(showEditCellBottomSheet = false) }
    }

    fun navigateTimetableEditor() {
        mviStore.postSideEffect(TimetableSideEffect.NavigateTimetableEditor)
    }

    fun navigateTimetableList() {
        mviStore.postSideEffect(TimetableSideEffect.NavigateTimetableList)
    }

    fun navigateAddTimetableCell() {
        val state = mviStore.uiState.value
        if (state.timetable == null) {
            mviStore.postSideEffect(TimetableSideEffect.ShowNeedCreateTimetableToast)
        } else {
            mviStore.postSideEffect(TimetableSideEffect.NavigateAddTimetableCell)
        }
    }
}
