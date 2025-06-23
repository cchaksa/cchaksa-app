package com.chukchukhaksa.mobile.common.designsystem.component.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayDA
import com.chukchukhaksa.mobile.common.designsystem.theme.Primary
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.suwikiClickable

@Composable
fun SuwikiOutlinedChip(
  modifier: Modifier = Modifier,
  text: String,
  isChecked: Boolean,
  onClick: () -> Unit = {},
) {
  val (borderLineColor, contentColor) = if (isChecked) {
    Primary to Primary
  } else {
    GrayDA to Gray95
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(5.dp))
      .suwikiClickable(onClick = onClick)
      .background(color = White)
      .border(width = 1.dp, color = borderLineColor, shape = RoundedCornerShape(5.dp))
      .padding(vertical = 4.dp, horizontal = 6.dp),
  ) {
    Text(
      text = text,
      style = SuwikiTheme.typography.caption1,
      color = contentColor,
      modifier = Modifier
        .align(Alignment.Center),
    )
  }
}
