package com.chukchukhaksa.mobile.presentation.timetable.navigation.argument

import com.chukchukhaksa.mobile.common.model.Cell
import com.chukchukhaksa.mobile.common.model.OpenLecture
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableDay
import kotlinx.serialization.Serializable

@Serializable
data class CellEditorArgument(
  val oldCellId: String = "",
  val name: String = "",
  val professorName: String = "",
  val cellList: List<CellArgument> = emptyList(),
  val timetableCellColor: TimetableCellColor = TimetableCellColor.entries.shuffled().first(),
) {
  val isEditMode = oldCellId.isNotEmpty()
}

@Serializable
data class CellArgument(
  val location: String = "",
  val day: TimetableDay = TimetableDay.MON,
  val startPeriod: String = "",
  val endPeriod: String = "",
)

internal fun OpenLecture.toCellEditorArgument() = CellEditorArgument(
  name = name,
  professorName = professorName,
  cellList = originalCellList.map { it.toCellArgument() },
)

internal fun Cell.toCellArgument() = CellArgument(
  location = location,
  day = day,
  startPeriod = startPeriod.toString(),
  endPeriod = endPeriod.toString(),
)

internal fun TimetableCell.toCellEditorArgument() = CellEditorArgument(
  oldCellId = id,
  name = name,
  professorName = professor,
  cellList = listOf(
    CellArgument(
      location = location,
      day = day,
      startPeriod = startPeriod.toString(),
      endPeriod = endPeriod.toString(),
    ),
  ),
  timetableCellColor = color,
)
