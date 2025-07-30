package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.CCHaksaTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray6A
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.MINUTE60
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.timetableBorderWidth
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.timetableHeightPerHour

@Composable
internal fun EmptyCell(
    modifier: Modifier = Modifier,
    isLeftTopTimetable: Boolean = false,
    isLeftBottomTimetable: Boolean = false,
    isRightTopTimetable: Boolean = false,
    isRightBottomTimetable: Boolean = false,
    minute: Int = MINUTE60,
    text: String? = null,
) {
    val radius = RoundedCornerShape(
      topStart = (if(isLeftTopTimetable) 12 else 0).dp,
      topEnd = (if(isRightTopTimetable) 12 else 0).dp,
      bottomStart = (if(isLeftBottomTimetable) 12 else 0).dp,
      bottomEnd = (if(isRightBottomTimetable) 12 else 0).dp,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(timetableHeightPerHour * minute / MINUTE60)
            .border(width = timetableBorderWidth, color = GrayF6, shape = radius)
            .background(White)
            .padding(timetableBorderWidth),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (text != null) {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = CCHaksaTheme.typography.bodyXs,
                color = Gray400,
            )
        }
    }
}
