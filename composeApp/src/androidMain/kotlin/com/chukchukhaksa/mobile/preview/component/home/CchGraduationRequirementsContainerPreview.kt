package com.chukchukhaksa.mobile.preview.component.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.presentation.home.component.CchGraduationRequirementsContainer

//@Preview(heightDp = 131)
@Preview
@Composable
fun CchGraduationRequirementsContainerPreview() {
  CchTheme {
    CchGraduationRequirementsContainer(
      major = "정보통신학부",
      gradeLevel = 18,
      totalEarnedCredits = 109,
      requiredCredits = 130,
      onClickGraduationProgress = {},
    )
  }
}
