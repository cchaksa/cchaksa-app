package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.component.dialog.SuwikiDialog
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme

@Preview
@Composable
fun DialogPreview() {
    SuwikiTheme {
        SuwikiDialog(
            headerText = "Header text",
            bodyText = "Body text",
            confirmButtonText = "Action 2",
            dismissButtonText = "Action 1",
            onDismissRequest = { /*TODO*/ },
            onClickConfirm = { /*TODO*/ },
        ) {
        }
    }
}