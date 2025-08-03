package com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput

import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.semesterList
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.Semester


data class TimetableNameInputState(
    val name: String = "",
    val semester: Semester = Semester("", "")
) {
    val buttonEnabled = name.isNotEmpty()
}

internal fun TimetableEditorArgument.toState() = TimetableNameInputState(
    name = name,
    semester = Semester(year, semester),
)

sealed interface TimetableNameInputSideEffect {
    data object NavigateTimetable : TimetableNameInputSideEffect
    data object PopBackStack : TimetableNameInputSideEffect
    data object NeedSelectSemesterToast : TimetableNameInputSideEffect
    data class HandleException(val throwable: Throwable) : TimetableNameInputSideEffect
}
