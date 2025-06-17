package com.chukchukhaksa.mobile.presentation.timetable.timetable

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType

data class TimetableState(
  val timetable: Timetable? = null,
  val cellType: TimetableCellType = TimetableCellType.CLASSNAME_PROFESSOR_LOCATION,
  val selectedCell: TimetableCell = TimetableCell(color = TimetableCellColor.GRAY_DARK),
  val showEditCellBottomSheet: Boolean = false,
  val showTimetableEmptyColumn: Boolean? = null,
  val showSelectCellTypeBottomSheet: Boolean = false,
)

sealed interface TimetableSideEffect {
  data object ShowNeedCreateTimetableToast : TimetableSideEffect
  data object NavigateAddTimetableCell : TimetableSideEffect
  data object NavigateTimetableEditor : TimetableSideEffect
  data object NavigateTimetableList : TimetableSideEffect
  data class NavigateCellEditor(val argument: CellEditorArgument) : TimetableSideEffect
  data class HandleException(val throwable: Throwable) : TimetableSideEffect
}
