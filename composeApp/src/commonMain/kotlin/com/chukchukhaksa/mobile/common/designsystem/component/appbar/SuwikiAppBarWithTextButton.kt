package com.chukchukhaksa.mobile.common.designsystem.component.appbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_appbar_arrow_left
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.Primary
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.suwikiClickable
import org.jetbrains.compose.resources.painterResource

@Composable
fun SuwikiAppBarWithTextButton(
  modifier: Modifier = Modifier,
  buttonText: String = "",
  onClickBack: () -> Unit = {},
  onClickTextButton: () -> Unit = {},
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .background(White)
      .padding(vertical = 15.dp, horizontal = 18.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Icon(
      painter = painterResource(Res.drawable.ic_appbar_arrow_left),
      contentDescription = "",
      tint = Gray95,
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .suwikiClickable(onClick = onClickBack)
        .padding(vertical = 2.dp, horizontal = 6.5.dp),
    )
    Text(
      modifier = Modifier
        .suwikiClickable(onClick = onClickTextButton)
        .padding(vertical = 4.dp, horizontal = 8.dp),
      color = Primary,
      style = SuwikiTheme.typography.body6,
      text = buttonText,
    )
  }
}
