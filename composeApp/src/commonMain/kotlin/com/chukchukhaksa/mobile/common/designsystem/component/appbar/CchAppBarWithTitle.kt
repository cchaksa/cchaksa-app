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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_appbar_arrow_left_chukchuk
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun CchAppBarWithTitle(
  modifier: Modifier = Modifier,
  title: String,
  onClickBackButton: () -> Unit,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .padding(14.dp)
  ) {
    Row(
      modifier = Modifier
        .wrapContentWidth()
        .height(24.dp)
        .clickable { onClickBackButton() },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Image(
        painter = painterResource(resource = Res.drawable.ic_appbar_arrow_left_chukchuk),
        contentDescription = "",
      )
      Text(
        text = title,
        color = Black100,
        style = CchTheme.typography.bodyMdStrong,
      )
    }
  }
}
