package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable

import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.model.TimetableDay

internal val timetableHeightPerHour = 48.dp

internal val timetableBorderWidth = 0.5.dp

const val MINUTE60 = 60
const val MINUTE10 = 10

internal fun TimetableDay.toText(): String {
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
