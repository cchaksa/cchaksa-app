package com.chukchukhaksa.mobile.presentation.timetable.openlecture

import androidx.compose.runtime.Composable
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.word_none
import com.chukchukhaksa.mobile.common.model.Cell
import com.chukchukhaksa.mobile.common.model.OpenLecture
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.openlecture.model.SchoolLevel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

data class OpenLectureState(
    val searchValue: String = "",
    val openLectureList: PersistentList<OpenLecture> = persistentListOf(),
    val selectedOpenMajor: String = "전체",
    val showSchoolLevelBottomSheet: Boolean = false,
    val schoolLevel: SchoolLevel = SchoolLevel.ALL,
    val showSelectCellColorBottomSheet: Boolean = false,
    val selectedTimetableCellColor: TimetableCellColor = TimetableCellColor.BROWN,
    val isLoading: Boolean = false,
    val lastUpdatedDate: String? = null,
)

@Composable
fun List<Cell>.toText(): String {
    return if (isEmpty()) {
        stringResource(Res.string.word_none)
    } else {
        joinToString(", ") { cell ->
            val periods = (cell.startPeriod..cell.endPeriod).joinToString(",") { it.toString() }
            val day = cell.day.toText()
            "$day ${periods}교시 (${cell.location})"
        }
    }
}

fun TimetableDay.toText() = when (this) {
    TimetableDay.MON -> "월"
    TimetableDay.TUE -> "화"
    TimetableDay.WED -> "수"
    TimetableDay.THU -> "목"
    TimetableDay.FRI -> "금"
    TimetableDay.SAT -> "토"
    TimetableDay.E_LEARNING -> "이러닝"
}

sealed interface OpenLectureSideEffect {
    data object ScrollToTop : OpenLectureSideEffect
    data object PopBackStack : OpenLectureSideEffect
    data class NavigateOpenMajor(val selectedOpenMajor: String) : OpenLectureSideEffect
    data class NavigateCellEditor(val argument: CellEditorArgument) : OpenLectureSideEffect
    data object NavigateAddCustomTimetableCell : OpenLectureSideEffect
    data class HandleException(val throwable: Throwable) : OpenLectureSideEffect
    data class ShowOverlapCellToast(val msg: String) : OpenLectureSideEffect
    data object ShowSuccessAddCellToast : OpenLectureSideEffect
}
