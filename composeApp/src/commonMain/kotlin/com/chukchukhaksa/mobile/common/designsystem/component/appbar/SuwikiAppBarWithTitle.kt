package com.chukchukhaksa.mobile.common.designsystem.component.appbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import chukchukhaksa.composeapp.generated.resources.ic_appbar_close_mark
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.suwikiClickable
import org.jetbrains.compose.resources.painterResource

@Composable
fun SuwikiAppBarWithTitle(
  modifier: Modifier = Modifier,
  title: String? = null,
  showCloseIcon: Boolean = true,
  showBackIcon: Boolean = true,
  onClickBack: () -> Unit = {},
  onClickClose: () -> Unit = {},
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .background(White)
      .padding(top = 15.dp, bottom = 15.dp, start = 18.dp, end = 24.dp),
  ) {
    if (showBackIcon) {
      Icon(
        modifier = Modifier
          .align(Alignment.CenterStart)
          .size(24.dp)
          .clip(CircleShape)
          .suwikiClickable(onClick = onClickBack)
          .padding(vertical = 2.dp, horizontal = 6.5.dp),
        painter = painterResource(resource = Res.drawable.ic_appbar_arrow_left),
        contentDescription = "",
        tint = Gray95,
      )
    }

    if (title != null) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = title,
        style = SuwikiTheme.typography.header6,
      )
    }

    if (showCloseIcon) {
      Icon(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .size(24.dp)
          .clip(CircleShape)
          .suwikiClickable(onClick = onClickClose)
          .padding(3.dp),
        painter = painterResource(resource = Res.drawable.ic_appbar_close_mark),
        contentDescription = "",
        tint = Gray95,
      )
    }
  }
}
