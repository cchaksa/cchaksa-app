package com.chukchukhaksa.mobile.common.designsystem.component.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayDA
import com.chukchukhaksa.mobile.common.designsystem.theme.Primary
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple200
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable

@Composable
fun CchOutlinedChip(
  modifier: Modifier = Modifier,
  text: String,
  isChecked: Boolean,
  onClick: () -> Unit = {},
) {
  val (borderLineColor, contentColor) = if (isChecked) {
    Purple200 to Purple600
  } else {
    Gray200 to Gray400
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape((6.25).dp))
      .cchClickable(onClick = onClick)
      .background(color = if (isChecked) Purple200 else White100)
      .border(width = 1.dp, color = borderLineColor, shape = RoundedCornerShape((6.25).dp))
      .size(40.dp),
  ) {
    Text(
      text = text,
      style = if (isChecked) CchTheme.typography.bodySmStrong else CchTheme.typography.bodySm,
      color = contentColor,
      modifier = Modifier
        .align(Alignment.Center),
    )
  }
}
