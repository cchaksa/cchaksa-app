package com.chukchukhaksa.mobile.common.designsystem.component.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray600
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import org.jetbrains.compose.resources.painterResource
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_search
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100

enum class CchSearchTextFieldState {
  Before,
  Typing,
  After
}

data class CchSearchTextFieldColors(
  val beforeBorderColor: Color,
  val typingBorderColor: Color,
  val afterBorderColor: Color,
  val beforeIconColor: Color,
  val typingIconColor: Color,
  val afterIconColor: Color,
  val textColor: Color,
  val placeholderColor: Color,
  val backgroundColor: Color,
  val cursorColor: Color,
)

object CchSearchTextFieldDefaults {
  @Composable
  fun colors(
    beforeBorderColor: Color = Gray200,
    typingBorderColor: Color = Purple600,
    afterBorderColor: Color = Gray200,
    beforeIconColor: Color = Gray400,
    typingIconColor: Color = Gray600,
    afterIconColor: Color = Gray600,
    textColor: Color = Black100,
    placeholderColor: Color = Gray400,
    backgroundColor: Color = White100,
    cursorColor: Color = Purple600,
  ): CchSearchTextFieldColors = CchSearchTextFieldColors(
    beforeBorderColor = beforeBorderColor,
    typingBorderColor = typingBorderColor,
    afterBorderColor = afterBorderColor,
    beforeIconColor = beforeIconColor,
    typingIconColor = typingIconColor,
    afterIconColor = afterIconColor,
    textColor = textColor,
    placeholderColor = placeholderColor,
    backgroundColor = backgroundColor,
    cursorColor = cursorColor,
  )

  val shape: Shape = RoundedCornerShape(10.dp)
}

@Composable
fun CchSearchTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  enabled: Boolean = true,
  textStyle: TextStyle = CchTheme.typography.bodyMd,
  colors: CchSearchTextFieldColors = CchSearchTextFieldDefaults.colors(),
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  val isFocused by interactionSource.collectIsFocusedAsState()

  val state = when {
    isFocused -> CchSearchTextFieldState.Typing
    value.isNotEmpty() -> CchSearchTextFieldState.After
    else -> CchSearchTextFieldState.Before
  }

  val borderColor = when (state) {
    CchSearchTextFieldState.Before -> colors.beforeBorderColor
    CchSearchTextFieldState.Typing -> colors.typingBorderColor
    CchSearchTextFieldState.After -> colors.afterBorderColor
  }

  val iconColor = when (state) {
    CchSearchTextFieldState.Before -> colors.beforeIconColor
    CchSearchTextFieldState.Typing -> colors.typingIconColor
    CchSearchTextFieldState.After -> colors.afterIconColor
  }
  val shape = CchSearchTextFieldDefaults.shape

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier.fillMaxWidth(),
    enabled = enabled,
    singleLine = true,
    textStyle = textStyle.copy(color = colors.textColor),
    cursorBrush = SolidColor(colors.cursorColor),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    interactionSource = interactionSource,
    decorationBox = { innerTextField ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(shape)
          .background(colors.backgroundColor)
          .border(1.dp, borderColor, shape)
          .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier.weight(1f),
        ) {
          if (value.isEmpty() && placeholder != null) {
            Text(
              text = placeholder,
              style = textStyle,
              color = colors.placeholderColor,
            )
          }
          innerTextField()
        }
        Spacer(modifier = Modifier.width(5.dp))
        Icon(
          painter = painterResource(Res.drawable.ic_search),
          contentDescription = null,
          modifier = Modifier.size(24.dp),
          tint = iconColor,
        )
      }
    },
  )
}

