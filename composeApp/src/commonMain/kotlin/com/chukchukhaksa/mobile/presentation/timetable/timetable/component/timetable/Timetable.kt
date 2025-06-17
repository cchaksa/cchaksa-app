package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.ELearningCell
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.column.ClassColumn
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.column.TimeColumn
import kotlin.math.max

private const val MIN_MAX_PERIOD = 8

internal fun List<TimetableCell>.maxPeriod(): Int {
    return max((maxOfOrNull { it.endPeriod }?.plus(1)) ?: MIN_MAX_PERIOD, MIN_MAX_PERIOD)
}

@Composable
fun Timetable(
    modifier: Modifier = Modifier,
    type: TimetableCellType = TimetableCellType.CLASSNAME_PROFESSOR_LOCATION,
    timetable: Timetable,
    onClickTimetableCell: (TimetableCell) -> Unit = { _ -> },
) {
    val scrollState = rememberScrollState()

    val maxPeriod = timetable.cellList.maxPeriod()

    // TODO 리컴포지션 최적화 필요
    val cellGroupedByDay = timetable.cellList.groupBy { it.day }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = timetableBorderWidth, color = GrayF6)
            .verticalScroll(scrollState),
    ) {
        Row {
            TimeColumn(
                modifier = Modifier.weight(1f),
                maxPeriod = maxPeriod,
            )

            TimetableDay.entries
                .sortedBy { it.idx }
                .filter { it !in listOf(TimetableDay.SAT, TimetableDay.E_LEARNING) }
                .forEach { day ->
                    ClassColumn(
                        modifier = Modifier.weight(1f),
                        type = type,
                        day = day,
                        cellList = cellGroupedByDay[day] ?: emptyList(),
                        lastPeriod = maxPeriod,
                        onClickClassCell = onClickTimetableCell,
                    )
                }
        }

        cellGroupedByDay
            .filter { it.key in listOf(TimetableDay.SAT, TimetableDay.E_LEARNING) }
            .flatMap { it.value }
            .forEach { cell ->
                ELearningCell(
                    onClickClassCell = onClickTimetableCell,
                    cell = cell,
                )
            }

        Spacer(modifier = Modifier.size(100.dp))
    }
}
