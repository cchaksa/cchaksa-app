package com.chukchukhaksa.mobile.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.presentation.timetable.openlecture.OpenLectureScreen

@Preview
@Composable
fun OpenLectureScreenPreview() {
  CchTheme {
      OpenLectureScreen()
  }
}
