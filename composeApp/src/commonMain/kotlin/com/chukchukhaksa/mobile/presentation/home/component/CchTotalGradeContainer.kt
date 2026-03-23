package com.chukchukhaksa.mobile.presentation.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple500
import com.chukchukhaksa.mobile.common.designsystem.theme.White100

@Composable
fun CchTotalGradeContainer(
  modifier : Modifier = Modifier,
  totalEarnedCredits: Int,
  cumulativeGpa: Double,
  percentile: Double,
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(Black100)
      .padding(vertical = 16.dp, horizontal = 29.dp)
      .fillMaxWidth()
      .wrapContentHeight(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly
  ) {
    CchTotalGradeContainerItem(
      title = "취득학점",
      value = totalEarnedCredits.toString()
    )
    CchTotalGradeContainerDivider()
    CchTotalGradeContainerItem(
      title = "평점 평균",
      value = cumulativeGpa.toString()
    )
    CchTotalGradeContainerDivider()
    CchTotalGradeContainerItem(
      title = "백분위",
      value = percentile.toString()
    )
  }
}

@Composable
fun CchTotalGradeContainerItem(
  title: String,
  value: String,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = title,
      color = White100,
      style = CchTheme.typography.bodySm
    )
    Text(
      text = value,
      color = Purple500,
      style = CchTheme.typography.bodyExlg
    )
  }
}

@Composable
fun CchTotalGradeContainerDivider() {
  VerticalDivider(
    modifier = Modifier
      .padding(vertical = 17.dp)
      .height(27.dp),
    thickness = 1.dp,
    color = Color(0xFF505050)
  )
}
