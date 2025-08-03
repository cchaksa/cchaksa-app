package com.chukchukhaksa.mobile.common.designsystem.component.container

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_arrow_sm
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray100
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray600
import org.jetbrains.compose.resources.painterResource

@Composable
fun CchSelectionButton(
  modifier: Modifier = Modifier,
  title: String = "",
  onClick: () -> Unit = {},
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .background(Gray100)
      .padding(vertical = 8.dp, horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    Text(
      text = title,
      color = Gray600,
      style = CchTheme.typography.bodySm,
    )
    Icon(
      painter = painterResource(resource = Res.drawable.ic_arrow_sm),
      contentDescription = "",
      tint = Gray600
    )
  }
}
