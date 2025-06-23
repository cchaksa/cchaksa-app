package com.chukchukhaksa.mobile.presentation.timetable.timetableeditor

import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class TimetableEditorState(
    val name: String = "",
    val isSheetOpenSemester: Boolean = false,
    val selectedSemesterPosition: Int? = null,
) {
    val semester = selectedSemesterPosition?.let { semesterList.getOrNull(it) }
    val buttonEnabled = name.isNotEmpty()
}

internal fun TimetableEditorArgument.toState() = TimetableEditorState(
    name = name,
    selectedSemesterPosition = semesterList.indexOf(Semester(year, semester)),
)

val semesterList: PersistentList<Semester> = run {
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val semesterList = mutableListOf<Semester>()
    for (year in currentYear downTo currentYear - 3) {
        semesterList
            .run {
                add(Semester(year.toString(), "1"))
                add(Semester(year.toString(), "2"))
            }
    }

    semesterList.toPersistentList()
}

data class Semester(
    val year: String,
    val semester: String,
) {
    fun toText() = "${year}년 ${semester}학기"
}

sealed interface TimetableEditorSideEffect {
    data object PopBackStack : TimetableEditorSideEffect
    data object NeedSelectSemesterToast : TimetableEditorSideEffect
    data class HandleException(val throwable: Throwable) : TimetableEditorSideEffect
}
