package com.chukchukhaksa.mobile.preview.component.timetable

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.Timetable

@Preview
@Composable
fun TimetablePreview() {
    SuwikiTheme {
        Timetable(
            timetable = Timetable(
                createTime = 0L,
                year = "",
                semester = "",
                name = "프리뷰입니다 프리뷰입니다 프리뷰입니다",
                cellList = listOf(
                    TimetableCell(
                        name = "도전과 창조",
                        professor = "김수미",
                        location = "미래혁신관B202",
                        day = TimetableDay.E_LEARNING,
                        startPeriod = 7,
                        endPeriod = 8,
                        color = TimetableCellColor.GREEN,
                    ),
                    TimetableCell(
                        name = "도전과 창조",
                        professor = "김수미",
                        location = "미래혁신관B202",
                        day = TimetableDay.SAT,
                        startPeriod = 7,
                        endPeriod = 8,
                        color = TimetableCellColor.GREEN,
                    ),
                    TimetableCell(
                        name = "도전과 창조",
                        professor = "김수미",
                        location = "미래혁신관B202",
                        day = TimetableDay.E_LEARNING,
                        startPeriod = 0,
                        endPeriod = 0,
                        color = TimetableCellColor.GREEN,
                    ),
                    TimetableCell(
                        name = "도전과 창조",
                        professor = "김수미",
                        location = "미래혁신관B202",
                        day = TimetableDay.MON,
                        startPeriod = 7,
                        endPeriod = 8,
                        color = TimetableCellColor.GREEN,
                    ),
                    TimetableCell(
                        name = "도전과 창조",
                        professor = "김수미",
                        location = "미래혁신관B202",
                        day = TimetableDay.FRI,
                        startPeriod = 1,
                        endPeriod = 2,
                        color = TimetableCellColor.GREEN,
                    ),
                    TimetableCell(
                        name = "도전과 창조",
                        professor = "김수미",
                        location = "미래혁신관B202",
                        day = TimetableDay.FRI,
                        startPeriod = 3,
                        endPeriod = 4,
                        color = TimetableCellColor.PINK,
                    ),
                    TimetableCell(
                        name = "도전과 창조",
                        professor = "김수미",
                        location = "미래혁신관B202",
                        day = TimetableDay.FRI,
                        startPeriod = 6,
                        endPeriod = 6,
                        color = TimetableCellColor.BROWN,
                    ),
                ),
            ),
        )
    }
}