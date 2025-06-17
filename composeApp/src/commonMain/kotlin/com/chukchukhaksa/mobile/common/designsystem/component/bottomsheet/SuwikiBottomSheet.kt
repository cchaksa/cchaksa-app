package com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.suwikiClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuwikiBottomSheet(
  sheetState: SheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
  ),
  isSheetOpen: Boolean,
  onDismissRequest: () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) {
  if (isSheetOpen) {
    ModalBottomSheet(
      sheetState = sheetState,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      onDismissRequest = onDismissRequest,
      containerColor = White,
      dragHandle = null,
    ) {
      content()
    }
  }
}

@Composable
fun SuwikiMenuItem(
  modifier: Modifier = Modifier,
  title: String,
  onClick: () -> Unit = {},
) {
  Text(
    text = title,
    style = SuwikiTheme.typography.body5,
    color = Gray95,
    modifier = modifier
      .fillMaxWidth()
      .background(White)
      .suwikiClickable(onClick = onClick)
      .padding(vertical = 14.dp, horizontal = 24.dp),
  )
}
