package com.chukchukhaksa.mobile.common.designsystem.component.container

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun ChukChukSelectionContainer(
  modifier: Modifier = Modifier,
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val (containerBackground, textColor, borderColor) = if (isSelected) {
    Triple(Color(0xff2B2B2B), Color(0xff9F94FF), Color(0xff2B2B2B))
  } else {
    Triple(Color(0xffFFFFFF), Color(0xff2B2B2B), Color(0xFFE8E9EF))
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
      fontSize = 18.sp,
      textAlign = TextAlign.Start,
      fontWeight = FontWeight.Bold,
      style = TextStyle(
        letterSpacing = (-0.02).em,
        lineHeight = 1.5.em
      ),
    )
  }
}
