package com.chukchukhaksa.mobile.common.designsystem.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CCHaksaTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray300
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.White100

@Composable
fun ChukChukBasicButton(
  modifier: Modifier = Modifier,
  text: String,
  enable: Boolean,
  onClick: () -> Unit,
) {
  val (containerBackground, textColor) = if(enable) {
    Pair(Black100, White100)
  } else {
    Pair(Gray300, Gray400)
  }
  val clickableModifier = if (enable) {
    Modifier.clickable(onClick = onClick)
  } else {
    Modifier
  }

  Box(
    modifier = modifier
      .wrapContentHeight()
      .clip(RoundedCornerShape(10.dp))
      .then(clickableModifier)
      .background(containerBackground)
      .padding(18.dp),
  ) {
      Text(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        text = text,
        color = textColor,
        style = CCHaksaTheme.typography.bodyLgStrong,
        textAlign = TextAlign.Center,
      )
  }
}
