package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.ui.suwikiClickable
import com.chukchukhaksa.mobile.common.ui.timetableCellColorHexMap
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.timetableBorderWidth
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.timetableHeightPerHour

enum class TimetableCellType(val text: String) {
    CLASSNAME("수업명"),
    CLASSNAME_LOCATION("수업명, 장소"),
    CLASSNAME_PROFESSOR("수업명, 교수명"),
    CLASSNAME_PROFESSOR_LOCATION("수업명, 교수명, 장소"),
    ;

    companion object {
        fun getType(value: String?) = TimetableCellType
            .entries
            .find { it.name == value }
            ?: CLASSNAME_PROFESSOR_LOCATION
    }
}

fun TimetableCellColor.toHex(): Long = timetableCellColorHexMap[this]!!

@Composable
fun ClassCell(
    modifier: Modifier = Modifier,
    type: TimetableCellType = TimetableCellType.CLASSNAME_PROFESSOR_LOCATION,
    data: TimetableCell,
    onClick: (TimetableCell) -> Unit = { _ -> },
) {
    val (showProfessor, showLocation) = when (type) {
        TimetableCellType.CLASSNAME -> (false to false)
        TimetableCellType.CLASSNAME_LOCATION -> (false to true)
        TimetableCellType.CLASSNAME_PROFESSOR -> (true to false)
        TimetableCellType.CLASSNAME_PROFESSOR_LOCATION -> (true to true)
    }

    val height = (data.endPeriod - data.startPeriod + 1) * timetableHeightPerHour - 8.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .border(width = timetableBorderWidth, color = GrayF6)
            .padding(timetableBorderWidth)
            .background(Color(data.color.toHex()))
            .suwikiClickable {
                onClick(data)
            },
    ) {
        Text(
            style = SuwikiTheme.typography.caption3,
            text = data.name,
            color = White,
        )

        if (showProfessor) {
            Text(
                style = SuwikiTheme.typography.caption6,
                text = data.professor,
                color = White,
            )
        }

        if (showLocation) {
            Text(
                style = SuwikiTheme.typography.caption6,
                text = data.location,
                color = White,
            )
        }
    }
}
