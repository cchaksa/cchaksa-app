package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.SuwikiAppBarWithTextButton
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.SuwikiAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme

@Preview
@Composable
fun SuwikiAddClassAppBarPreview() {
  SuwikiTheme {
    SuwikiAppBarWithTextButton(
      buttonText = "text",
      onClickBack = { /*TODO*/ },
      onClickTextButton = { /*TODO*/ },
    )
  }
}

@Preview
@Composable
fun SuwikiAppBarPreview() {
  SuwikiTheme {
    SuwikiAppBarWithTitle(
      title = "타이틀",
      onClickBack = { /*TODO*/ },
      onClickClose = { /*TODO*/ },
    )
  }
}