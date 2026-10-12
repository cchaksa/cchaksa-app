package com.chukchukhaksa.mobile.common.designsystem.component.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray100
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray500

@Composable
fun CchBadge(
    modifier: Modifier = Modifier,
    text: String = "",
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(color = Gray100)
      .padding(vertical = 4.dp, horizontal = 6.dp),
  ) {
    Text(
      text = text,
      style = CchTheme.typography.bodyXxs,
      color = Gray500,
      modifier = Modifier.align(Alignment.Center),
    )
  }
}
