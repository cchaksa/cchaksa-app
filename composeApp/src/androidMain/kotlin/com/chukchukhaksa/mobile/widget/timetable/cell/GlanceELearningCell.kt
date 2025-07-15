package com.chukchukhaksa.mobile.widget.timetable.cell

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.widget.timetable.toGlanceText

@Composable
internal fun GlanceELearningCell(
  modifier: GlanceModifier = GlanceModifier,
  cell: TimetableCell,
) {
  val nameAndDay = "${cell.name} / ${cell.day.toGlanceText()}"
  val period = "(${cell.startPeriod} - ${cell.endPeriod})"

  val text = if (cell.startPeriod != 0 && cell.endPeriod != 0) {
    nameAndDay + period
  } else {
    nameAndDay
  }

  GlanceEmptyCell(
    modifier = modifier,
    text = text,
  )
}
