package com.chukchukhaksa.mobile.presentation.timetable.timetableeditor

import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.semesterList
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.Semester


data class TimetableEditorState(
    val name: String = "",
    val preName: String = "",
    val isSheetOpenSemester: Boolean = false,
    val preSelectedSemesterPosition: Int? = null,
    val selectedSemesterPosition: Int? = null,
) {
    val semester = selectedSemesterPosition?.let { semesterList.getOrNull(it) }
    val buttonEnabled = (name.isNotEmpty() && preName != name || preSelectedSemesterPosition != selectedSemesterPosition)
}

internal fun TimetableEditorArgument.toState() = TimetableEditorState(
    name = name,
    preName = name,
    selectedSemesterPosition = semesterList.indexOf(Semester(year, semester)),
    preSelectedSemesterPosition = semesterList.indexOf(Semester(year, semester)),
)

sealed interface TimetableEditorSideEffect {
    data object PopBackStack : TimetableEditorSideEffect
    data object ShowEditSaveToast : TimetableEditorSideEffect
    data class HandleException(val throwable: Throwable) : TimetableEditorSideEffect
}
