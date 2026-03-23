package com.chukchukhaksa.mobile.preview.component.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.presentation.home.component.CchProfileContainer

@Preview
@Composable
fun CchProfilePreview() {
  CchTheme {
    CchProfileContainer(
      name = "김척척",
      departmentName = "정보통신학부",
      studentCode = "17234032",
      currentSemester = 6,
      status = "재학 중"
    )
  }
}
