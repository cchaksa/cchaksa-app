package com.chukchukhaksa.mobile.preview.component.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.presentation.home.component.CchTotalGradeContainer

@Preview(heightDp = 93)
@Composable
fun CchTotalGradeContainerPreview() {
  CchTheme {
    CchTotalGradeContainer(
      totalEarnedCredits = 109,
      cumulativeGpa = 3.03,
      percentile = 83.2,
    )
  }
}
