package com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray300
import com.chukchukhaksa.mobile.common.designsystem.theme.White100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CchBottomSheet(
  onDismissRequest: () -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
  ),
  content: @Composable ColumnScope.() -> Unit,
) {
  ModalBottomSheet(
    sheetState = sheetState,
    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    onDismissRequest = onDismissRequest,
    containerColor = White100,
    dragHandle = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 18.dp, bottom = 20.dp),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier = Modifier
            .width(47.dp)
            .height(4.dp)
            .background(
              color = Gray300,
              shape = RoundedCornerShape(100.dp),
            ),
        )
      }
    },
  ) {
    content()
  }
}
