package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.column

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.MINUTE10
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.MINUTE60
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.ClassCell
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.EmptyCell
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.toText

internal fun TimetableCell.getStartAndEndMinute(): Pair<Int, Int> {
    val startMinute = (this.startPeriod + 8) * MINUTE60 + 3 * MINUTE10
    val endMinute = (this.endPeriod + 9) * MINUTE60 + 2 * MINUTE10
    return (startMinute to endMinute)
}

internal fun Int.endPeriodToMinute(): Int {
    return (this + 9) * MINUTE60
}

internal fun Int.isNotOnTime(): Boolean {
    return this % MINUTE60 != 0
}

@Composable
internal fun ClassColumn(
    modifier: Modifier = Modifier,
    day: TimetableDay,
    type: TimetableCellType = TimetableCellType.CLASSNAME_PROFESSOR_LOCATION,
    cellList: List<TimetableCell>,
    lastPeriod: Int,
    onClickClassCell: (TimetableCell) -> Unit = { _ -> },
) {
    val sortedCellList = cellList.sortedBy { it.startPeriod }
    Column(
        modifier = modifier,
    ) {
        EmptyCell(
            text = day.toText(),
        )

        var prevEndTime = 9 * MINUTE60
        sortedCellList.forEach { cell ->
            val (startMinute, endMinute) = cell.getStartAndEndMinute()
            FillEmptyTime(prevEndTime, startMinute)
            ClassCell(type = type, data = cell, onClick = onClickClassCell)
            prevEndTime = endMinute
        }

        FillEmptyTime(prevEndTime, lastPeriod.endPeriodToMinute())
    }
}

@Composable
fun FillEmptyTime(emptyStartTime: Int, emptyEndTime: Int) {
    var filledEmptyTime = emptyStartTime

    while (filledEmptyTime < emptyEndTime) {
        val insertEmptyTimeAmount = when {
            // 종료 시각까지 1시간이 안되는 경우, 종료 시각까지 남은 시간을 채운다.
            emptyEndTime - filledEmptyTime < MINUTE60 -> {
                emptyEndTime - filledEmptyTime
            }

            // 정각이 아닌 경우 정각까지 남은 시간을 채운다.
            filledEmptyTime.isNotOnTime() -> {
                MINUTE60 - filledEmptyTime % MINUTE60
            }

            // 그렇지 않다면 1시간을 채운다.
            else -> {
                MINUTE60
            }
        }

        EmptyCell(
            minute = insertEmptyTimeAmount,
        )

        filledEmptyTime += insertEmptyTimeAmount
    }
}
