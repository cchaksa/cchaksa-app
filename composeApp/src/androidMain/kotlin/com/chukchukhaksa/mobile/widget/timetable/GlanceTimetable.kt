package com.chukchukhaksa.mobile.widget.timetable

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.width
import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.maxPeriod
import com.chukchukhaksa.mobile.widget.timetable.cell.GlanceELearningCell
import com.chukchukhaksa.mobile.widget.timetable.column.GlanceClassColumn
import com.chukchukhaksa.mobile.widget.timetable.column.GlanceTimeColumn

@Composable
@GlanceComposable
fun GlanceTimetable(
  modifier: GlanceModifier = GlanceModifier,
  size: Dp,
  type: TimetableCellType = TimetableCellType.CLASSNAME_PROFESSOR_LOCATION,
  timetable: Timetable,
) {
  val maxPeriod = timetable.cellList.maxPeriod()
  val cellGroupedByDay = timetable.cellList.groupBy { it.day }

  Column(
    modifier = modifier.fillMaxSize()
  ) {
    Row {
      GlanceTimeColumn(
        modifier = GlanceModifier.width(size / 6),
        maxPeriod = maxPeriod,
      )

      TimetableDay.entries
        .sortedBy { it.idx }
        .filter { it !in listOf(TimetableDay.SAT, TimetableDay.E_LEARNING) }
        .forEach { day ->
          GlanceClassColumn(
            modifier = GlanceModifier.width(size / 6),
            type = type,
            day = day,
            cellList = cellGroupedByDay[day] ?: emptyList(),
            lastPeriod = maxPeriod,
          )
        }
    }

    cellGroupedByDay
      .filter { it.key in listOf(TimetableDay.SAT, TimetableDay.E_LEARNING) }
      .flatMap { it.value }
      .forEach { cell ->
        GlanceELearningCell(
          modifier = GlanceModifier.fillMaxWidth(),
          cell = cell,
        )
      }
  }
}
