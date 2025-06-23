package com.chukchukhaksa.mobile.preview.component.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.TimetableEmptyColumn

@Preview
@Composable
fun TimetableEmptyColumnPreview() {
  SuwikiTheme {
    TimetableEmptyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(White),
    )
  }
}