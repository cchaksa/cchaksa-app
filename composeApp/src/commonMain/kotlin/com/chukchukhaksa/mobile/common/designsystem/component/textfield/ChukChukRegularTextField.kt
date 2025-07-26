package com.chukchukhaksa.mobile.common.designsystem.component.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.component.button.TextFieldClearButton
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CCHaksaTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.White100

@Composable
fun ChukChukRegularTextField(
  modifier: Modifier = Modifier,
  value: String = "",
  onValueChanged: (String) -> Unit = {},
  onClearButtonClicked: () -> Unit = {},
  placeholder: String = "",
) {
  val (borderColor, textColor, textStyle) = if(value.isEmpty()) {
    Triple(Gray200, Gray400, CCHaksaTheme.typography.bodyLg)
  } else {
    Triple(Purple600, Black100, CCHaksaTheme.typography.bodyLgStrong)
  }
  BasicTextField(
    value = value,
    onValueChange = onValueChanged,
    modifier = modifier.fillMaxWidth(),
    singleLine = true,
    textStyle = textStyle,
    cursorBrush = SolidColor(Purple600),
    keyboardOptions = KeyboardOptions.Default,
    decorationBox = { innerTextField ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(color = White100)
          .border(width = 1.dp, color = borderColor, RoundedCornerShape(10.dp))
          .padding(top = 18.dp, bottom = 18.dp, start = 20.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(modifier = Modifier.weight(1f)) {
          innerTextField()
          if (value.isEmpty()) {
            Text(
              text = placeholder,
              color = textColor,
              style = textStyle
            )
          }
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextFieldClearButton(onClick = onClearButtonClicked)
      }
    },
  )
}
