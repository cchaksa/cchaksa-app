package com.chukchukhaksa.mobile.common.designsystem.component.button

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_textfield_clear
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.ui.suwikiClickable
import org.jetbrains.compose.resources.painterResource

@Composable
fun TextFieldClearButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Icon(
    modifier = modifier
      .clip(CircleShape)
      .suwikiClickable(onClick = onClick),
    painter = painterResource(resource = Res.drawable.ic_textfield_clear),
    tint = Gray95,
    contentDescription = "",
  )
}
