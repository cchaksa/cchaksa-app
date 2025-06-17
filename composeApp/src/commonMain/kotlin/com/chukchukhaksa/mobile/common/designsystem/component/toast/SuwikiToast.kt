package com.chukchukhaksa.mobile.common.designsystem.component.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SuwikiToast(
  visible: Boolean,
  message: String,
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 70.dp),
      contentAlignment = Alignment.BottomCenter,
    ) {
      SuwikiToastContent(
        modifier = Modifier.imePadding(),
        message = message,
      )
    }
  }
}

@Composable
private fun SuwikiToastContent(
  modifier: Modifier = Modifier,
  message: String,
) {
  Surface(
    modifier = modifier
      .wrapContentSize()
      .background(
        color = Gray95,
        shape = RoundedCornerShape(25.dp),
      ),
    color = Color.Transparent,
  ) {
    Text(
      text = message,
      textAlign = TextAlign.Center,
      style = SuwikiTheme.typography.body5,
      color = White,
      modifier = Modifier.padding(16.dp, 10.dp),
    )
  }
}

@Preview
@Composable
fun SuwikiToastPreview() {
  SuwikiTheme {
    SuwikiToastContent(
      message = "text",
    )
  }
}
