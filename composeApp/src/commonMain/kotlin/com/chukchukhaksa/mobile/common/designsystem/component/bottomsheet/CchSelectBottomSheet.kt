package com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.ui.cchClickable
import kotlinx.collections.immutable.PersistentList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CchSelectBottomSheet(
  onDismissRequest: () -> Unit,
  onClickItem: (Int) -> Unit,
  itemList: PersistentList<String>,
  selectedPosition: Int?,
  title: String? = null,
) {
  CchBottomSheet(
    sheetState = rememberModalBottomSheetState(
      skipPartiallyExpanded = true,
    ),
    onDismissRequest = onDismissRequest,
  ) {
    CchSelectBottomSheetContent(
      onClickAlignBottomSheetItem = onClickItem,
      bottomSheetTitle = title,
      itemList = itemList,
      selectedPosition = selectedPosition,
    )
  }
}

@Composable
private fun CchSelectContainer(
  text: String,
  modifier: Modifier = Modifier,
  isChecked: Boolean = false,
  onClick: () -> Unit = {},
) {
  val textColor = if (isChecked) Purple600 else Black100
  Text(
    text = text,
    color = textColor,
    style = CchTheme.typography.bodyMdStrong,
    modifier = modifier
      .fillMaxWidth()
      .cchClickable(
        onClick = onClick,
      )
      .padding(horizontal = 20.dp, vertical = 16.dp),
  )
}

@Composable
fun CchSelectBottomSheetContent(
  selectedPosition: Int?,
  onClickAlignBottomSheetItem: (Int) -> Unit = {},
  bottomSheetTitle: String? = null,
  itemList: List<String>,
) {
  Column {
    if (bottomSheetTitle != null) {
      Text(
        text = bottomSheetTitle,
        style = CchTheme.typography.bodySm,
        color = Gray400,
        modifier = Modifier.padding(bottom = 4.dp, start = 20.dp, end = 20.dp),
      )
    }

    itemList.forEachIndexed { index, item ->
      val isChecked = index == selectedPosition
      CchSelectContainer(
        text = item,
        isChecked = isChecked,
        onClick = {
          if (!isChecked) {
            onClickAlignBottomSheetItem(index)
          }
        },
      )
    }
  }
}
