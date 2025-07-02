package com.chukchukhaksa.mobile.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.White

@Composable
fun ChukChukWidgetEmptyScreen() {
  Column(
    modifier = GlanceModifier
      .fillMaxSize()
      .background(ColorProvider(White)),
    verticalAlignment = Alignment.CenterVertically,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
//      text = context.getString(R.string.timetable_screen_create_timetable),
      text = "시간표를 만들어 일정을 관리해보세요.",
      style = TextStyle(
        color = ColorProvider(Gray95),
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
      ),
    )
  }
}
