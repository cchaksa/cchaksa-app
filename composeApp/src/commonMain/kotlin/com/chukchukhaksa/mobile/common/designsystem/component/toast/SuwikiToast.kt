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
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CchToast(
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
      CchToastContent(
        modifier = Modifier.imePadding(),
        message = message,
      )
    }
  }
}

@Composable
private fun CchToastContent(
  modifier: Modifier = Modifier,
  message: String,
) {
  Surface(
    modifier = modifier
      .wrapContentSize()
      .background(
        color = Black100.copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp),
      ),
    color = Color.Transparent,
  ) {
    Text(
      text = message,
      textAlign = TextAlign.Center,
      style = CchTheme.typography.bodyMd,
      color = White100,
      modifier = Modifier.padding(24.dp, 16.dp),
    )
  }
}

@Preview
@Composable
fun CchToastPreview() {
  CchTheme {
    CchToastContent(
      message = "text",
    )
  }
}
