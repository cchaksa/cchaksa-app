package com.chukchukhaksa.mobile.common.designsystem.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun ChukChukBasicButton(
  modifier: Modifier = Modifier,
  text: String,
  enable: Boolean,
  onClick: () -> Unit,
) {
  val (containerBackground, textColor) = if(enable) {
    Pair(Color(0xff2B2B2B), Color(0xffFFFFFF))
  } else {
    Pair(Color(0xffD9D9E3), Color(0xffA3A2B1))
  }
  val clickableModifier = if (enable) {
    Modifier.clickable(onClick = onClick)
  } else {
    Modifier
  }

  Box(
    modifier = modifier
      .height(64.dp)
      .clip(RoundedCornerShape(10.dp))
      .then(clickableModifier)
      .background(containerBackground)
      .padding(18.dp),
  ) {
      Text(
        modifier = Modifier.fillMaxSize(),
        text = text,
        color = textColor,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        style = TextStyle(
          letterSpacing = (-0.02).em,
          lineHeight = 1.5.em,
        ),
      )
  }
}
