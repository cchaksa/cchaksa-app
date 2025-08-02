package com.chukchukhaksa.mobile.common.designsystem.component.container

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple500
import com.chukchukhaksa.mobile.common.designsystem.theme.White100

@Composable
fun ChukChukSelectionContainer(
  modifier: Modifier = Modifier,
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val (containerBackground, textColor, borderColor) = if (isSelected) {
    Triple(Black100, Purple500, Black100)
  } else {
    Triple(White100, Black100, Gray200)
  }
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(containerBackground)
      .border(1.dp, borderColor, RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .padding(vertical = 21.dp, horizontal = 18.dp),
  ) {
    Text(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterStart),
      text = text,
      color = textColor,
//      textAlign = TextAlign.Start,
      style = CchTheme.typography.bodyLgStrong,
    )
  }
}
