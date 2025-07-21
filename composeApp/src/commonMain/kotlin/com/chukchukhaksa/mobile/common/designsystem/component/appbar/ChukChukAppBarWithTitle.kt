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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_appbar_arrow_left_chukchuk
import org.jetbrains.compose.resources.painterResource

@Composable
fun ChukChukAppBarWithTitle(
  title: String,
  onClickBackButton: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .padding(14.dp)
  ) {
    Row(
      modifier = Modifier
        .wrapContentWidth()
        .height(24.dp)
        .clickable { onClickBackButton() },
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Image(
        painter = painterResource(resource = Res.drawable.ic_appbar_arrow_left_chukchuk),
        contentDescription = "",
      )
      Text(
        text = title,
        fontSize = 16.sp,
        color = Color(0xff2B2B2B),
        fontWeight = FontWeight.Bold,
        style = TextStyle(letterSpacing = (-0.02).em),
      )
    }
  }
}
