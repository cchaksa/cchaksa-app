package com.chukchukhaksa.mobile.presentation.timetable.semesterselect

import androidx.lifecycle.ViewModel
import com.chukchukhaksa.mobile.common.ui.mviStore

class SemesterSelectViewModel : ViewModel() {
    val mviStore = mviStore<SemesterSelectState, SemesterSelectSideEffect>(SemesterSelectState())

    fun updateSelectedSemesterIndex(idx: Int) {
        mviStore.setState {
            copy(selectSemesterIndex = if (mviStore.uiState.value.selectSemesterIndex == idx) null else idx)
        }
        mviStore.setState { copy(nextButtonEnable = mviStore.uiState.value.selectSemesterIndex != null) }

    }

    fun updateSelectSemester(semester: Semester?) = mviStore.setState { copy(selectSemester = semester) }

    fun navigateTimetableNameInput(semester: Semester?) {
      if (semester != null) {
        mviStore.postSideEffect(SemesterSelectSideEffect.NavigateTimetableNameInput(semester.toTimetableEditorArgument()))
      }
    }

}
