package com.chukchukhaksa.mobile.preview.component.timetable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.ClassCell
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType

@Preview
@Composable
fun TimetableClassCellPreview() {
    SuwikiTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ClassCell(
                modifier = Modifier.width(50.dp),
                type = TimetableCellType.CLASSNAME,
                data = TimetableCell(
                    name = "도전과 창조",
                    professor = "김수미",
                    location = "미래혁신관B202",
                    startPeriod = 1,
                    endPeriod = 3,
                    color = TimetableCellColor.GREEN,
                ),
            )

            ClassCell(
                modifier = Modifier.width(50.dp),
                type = TimetableCellType.CLASSNAME_PROFESSOR,
                data = TimetableCell(
                    name = "도전과 창조",
                    professor = "김수미",
                    location = "미래혁신관B202",
                    startPeriod = 1,
                    endPeriod = 3,
                    color = TimetableCellColor.GREEN,
                ),
            )

            ClassCell(
                modifier = Modifier.width(50.dp),
                type = TimetableCellType.CLASSNAME_LOCATION,
                data = TimetableCell(
                    name = "도전과 창조",
                    professor = "김수미",
                    location = "미래혁신관B202",
                    startPeriod = 1,
                    endPeriod = 3,
                    color = TimetableCellColor.GREEN,
                ),
            )

            ClassCell(
                modifier = Modifier.width(50.dp),
                type = TimetableCellType.CLASSNAME_PROFESSOR_LOCATION,
                data = TimetableCell(
                    name = "도전과 창조",
                    professor = "김수미",
                    location = "미래혁신관B202",
                    startPeriod = 1,
                    endPeriod = 3,
                    color = TimetableCellColor.GREEN,
                ),
            )
        }
    }
}