package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
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
    // 토요일과 이러닝 수업은 그리드가 아닌 하단 목록으로 표시되므로 시간축 길이에 반영하지 않는다.
    val gridCellList = filterNot { it.day == TimetableDay.SAT || it.day == TimetableDay.E_LEARNING }
    return max((gridCellList.maxOfOrNull { it.endPeriod }?.plus(1)) ?: MIN_MAX_PERIOD, MIN_MAX_PERIOD)
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
    val eLearningCellList =  cellGroupedByDay.filter { it.key in listOf(TimetableDay.SAT, TimetableDay.E_LEARNING) }
    val timeWeight = 0.06f
    val classWeight = (1 - timeWeight) / TimetableDay.entries.filterNot { it == TimetableDay.SAT || it == TimetableDay.E_LEARNING }.size

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .border(width = timetableBorderWidth, color = Gray200, shape = RoundedCornerShape(12.dp))
            .verticalScroll(scrollState),
    ) {
        Row {
            TimeColumn(
                modifier = Modifier.weight(timeWeight),
                maxPeriod = maxPeriod,
                isHasELearning = eLearningCellList.isNotEmpty(),
            )

            TimetableDay.entries
                .sortedBy { it.idx }
                .filter { it !in listOf(TimetableDay.SAT, TimetableDay.E_LEARNING) }
                .forEach { day ->
                    ClassColumn(
                        modifier = Modifier.weight(classWeight),
                        type = type,
                        day = day,
                        cellList = cellGroupedByDay[day] ?: emptyList(),
                        lastPeriod = maxPeriod,
                        isHasELearning = eLearningCellList.isNotEmpty(),
                        onClickClassCell = onClickTimetableCell,
                    )
                }
        }

      eLearningCellList
            .flatMap { it.value }
            .forEach { cell ->
                ELearningCell(
                    onClickClassCell = onClickTimetableCell,
                    cell = cell,
                    isLast = cell == eLearningCellList.flatMap { it.value }.last(),
                )
            }
    }
}
