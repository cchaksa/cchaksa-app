package com.chukchukhaksa.mobile.common.designsystem.component.appbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_appbar_arrow_left_chukchuk
import chukchukhaksa.composeapp.generated.resources.ic_timetable_add
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.ui.cchClickable
import org.jetbrains.compose.resources.painterResource

@Composable
fun CchAppBarWithTitle(
  modifier: Modifier = Modifier,
  title: String,
  isShowAddButton: Boolean = false,
  onClickBackButton: () -> Unit,
  onClickAdd: () -> Unit = {},
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .padding(top = 14.dp, bottom = 14.dp, start = 14.dp, end = 20.dp)
  ) {
    Row(
      modifier = Modifier
        .wrapContentWidth()
        .height(24.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Image(
        modifier = Modifier
          .clip(CircleShape)
          .cchClickable(onClick = onClickBackButton),
        painter = painterResource(resource = Res.drawable.ic_appbar_arrow_left_chukchuk),
        contentDescription = "",
      )
      Text(
        text = title,
        color = Black100,
        style = CchTheme.typography.bodyMdStrong,
      )
    }
    if (isShowAddButton) {
      Icon(
        modifier = Modifier
          .clip(CircleShape)
          .align(Alignment.CenterEnd)
          .cchClickable(onClick = onClickAdd),
        painter = painterResource(Res.drawable.ic_timetable_add),
        contentDescription = "",
        tint = Black100,
      )
    }
  }
}
