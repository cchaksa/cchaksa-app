package com.chukchukhaksa.mobile.preview.component.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.presentation.home.component.CchGraduationRequirementsContainer
import com.chukchukhaksa.mobile.presentation.home.component.CchSemesterGradeButton

@Preview
@Composable
fun CchSemesterGradeButtonPreview() {
  CchTheme {
    CchSemesterGradeButton(
      startSemester = "1학년 1학기",
      endSemester = "3학년 1학기",
      onClick = {}
    )
  }
}
