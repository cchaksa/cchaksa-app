package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.column

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.EmptyCell

@Composable
internal fun TimeColumn(
  modifier: Modifier = Modifier,
  maxPeriod: Int,
  isHasELearning: Boolean = false,
) {
  Column(
    modifier = modifier,
  ) {
    EmptyCell(
      modifier = Modifier.height(30.dp),
      isLeftTopTimetable = true
    )

    for (time in 1..maxPeriod) {
      EmptyCell(
        text = "${time + 8}",
        isLeftBottomTimetable = (time == maxPeriod) && !isHasELearning,
      )
    }
  }
}
