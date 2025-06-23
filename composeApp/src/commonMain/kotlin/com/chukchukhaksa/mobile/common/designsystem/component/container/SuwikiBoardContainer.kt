package com.chukchukhaksa.mobile.common.designsystem.component.container

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Black
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.suwikiClickable

@Composable
fun SuwikiBoardContainer(
  modifier: Modifier = Modifier,
  titleText: String,
  dateText: String,
  onClick: () -> Unit = {},
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .suwikiClickable(onClick = onClick)
      .background(White)
      .padding(24.dp, 15.dp),
  ) {
    Text(
      text = titleText,
      style = SuwikiTheme.typography.header6,
      color = Black,
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = dateText,
      style = SuwikiTheme.typography.caption6,
      color = Gray95,
    )
  }
}
