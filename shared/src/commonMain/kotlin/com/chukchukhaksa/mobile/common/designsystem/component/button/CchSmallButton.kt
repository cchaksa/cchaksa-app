package com.chukchukhaksa.mobile.common.designsystem.component.button

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.ui.cchClickable

@Composable
fun CchSmallButton(
  modifier: Modifier = Modifier,
  text: String,
  textColor: Color,
  onClick: () -> Unit,
) {
  Text(
    modifier = modifier.cchClickable { onClick() },
    text = text,
    style = CchTheme.typography.bodySm,
    color = textColor,
  )
}
