package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.timetableBorderWidth
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.toText

@Composable
internal fun ELearningCell(
    modifier: Modifier = Modifier,
    cell: TimetableCell,
    isLast: Boolean = false,
    onClickClassCell: (TimetableCell) -> Unit = { _ -> },
) {
    val nameAndDay = "${cell.name} / ${cell.day.toText()}"
    val period = "(${cell.startPeriod} - ${cell.endPeriod})"

    val text = if (cell.startPeriod != 0 && cell.endPeriod != 0) {
        nameAndDay + period
    } else {
        nameAndDay
    }
    val radius = RoundedCornerShape(
      bottomStart = if(isLast) 12.dp else 0.dp,
      bottomEnd = if(isLast) 12.dp else 0.dp,
    )
    Column(
      modifier = modifier
        .fillMaxWidth()
        .clip(shape = radius)
        .border(width = timetableBorderWidth, color = GrayF6)
        .cchClickable {
          onClickClassCell(cell)
        }
        .background(White)
        .padding(vertical = 8.dp, horizontal = 20.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = CchTheme.typography.bodyXs,
        color = Gray400,
      )
    }
}
