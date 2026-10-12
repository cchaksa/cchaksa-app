package com.chukchukhaksa.mobile.widget.timetable

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.model.TimetableDay

internal val glanceTimetableHeightPerHour = 48.dp

internal val glanceTimetableBorderWidth = 0.5.dp

@Composable
internal fun TimetableDay.toGlanceText(): String {
  return when (this) {
    TimetableDay.MON -> "월"
    TimetableDay.TUE -> "화"
    TimetableDay.WED -> "수"
    TimetableDay.THU -> "목"
    TimetableDay.FRI -> "금"
    TimetableDay.SAT -> "토"
    TimetableDay.E_LEARNING -> "이러닝"
  }
}
