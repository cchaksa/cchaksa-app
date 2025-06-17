package com.chukchukhaksa.mobile.common.designsystem.component.container

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_dropdown_arrow_down
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun SuwikiSelectionContainer(
  modifier: Modifier = Modifier,
  title: String = "",
  onClick: () -> Unit = {},
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .background(GrayF6)
      .padding(
        horizontal = 9.dp,
        vertical = 6.dp,
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      color = Gray95,
      style = SuwikiTheme.typography.body6,
    )
    Image(
      painter = painterResource(resource = Res.drawable.ic_dropdown_arrow_down),
      contentDescription = "",
    )
  }
}
