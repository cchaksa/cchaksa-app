package com.chukchukhaksa.mobile.preview.component.timetable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.EmptyCell
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.column.ClassColumn

@Preview
@Composable
fun TimetableCellColumnPreview() {
    SuwikiTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                EmptyCell(text = "")
                repeat(8) {
                    EmptyCell(text = "${it + 9}")
                }
            }

            repeat(5) {
                ClassColumn(
                    modifier = Modifier.weight(1f),
                    day = TimetableDay.FRI,
                    cellList = listOf(
                        TimetableCell(
                            name = "도전과 창조",
                            professor = "김수미",
                            location = "미래혁신관B202",
                            startPeriod = 1,
                            endPeriod = 2,
                            color = TimetableCellColor.GREEN,
                        ),
                        TimetableCell(
                            name = "도전과 창조",
                            professor = "김수미",
                            location = "미래혁신관B202",
                            startPeriod = 3,
                            endPeriod = 4,
                            color = TimetableCellColor.PINK,
                        ),
                        TimetableCell(
                            name = "도전과 창조",
                            professor = "김수미",
                            location = "미래혁신관B202",
                            startPeriod = 6,
                            endPeriod = 6,
                            color = TimetableCellColor.BROWN,
                        ),
                    ),
                    lastPeriod = 8,
                )
            }
        }
    }
}