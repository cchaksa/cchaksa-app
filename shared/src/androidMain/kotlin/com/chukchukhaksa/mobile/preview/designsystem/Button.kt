package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedLargeButton
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedMediumButton
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedSmallButton
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiOutlinedButton
import com.chukchukhaksa.mobile.common.designsystem.component.button.TextFieldClearButton
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme

@Preview
@Composable
fun SuwikiContainedButtonPreview() {
    SuwikiTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SuwikiContainedLargeButton(enabled = true, text = "버튼")

            SuwikiContainedLargeButton(enabled = false, text = "버튼")

            SuwikiContainedMediumButton(enabled = true, text = "버튼")

            SuwikiContainedMediumButton(enabled = false, text = "버튼")

            SuwikiContainedSmallButton(text = "버튼")
        }
    }
}

@Preview
@Composable
fun SuwikiOutlinedButtonPreview() {
    SuwikiTheme {
        SuwikiOutlinedButton(text = "버튼")
    }
}

@Preview
@Composable
fun TextFieldClearButtonPreview() {
    SuwikiTheme {
        TextFieldClearButton(onClick = {})
    }
}