package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.SuwikiRegularTextField
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.SuwikiSmallTextField
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun SuwikiRegularTextFieldPreview() {
    SuwikiTheme {
        var normalValue by remember {
            mutableStateOf("")
        }

        var errorValue by remember {
            mutableStateOf("")
        }

        Column(
            modifier = Modifier.background(White),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SuwikiRegularTextField(
                label = "라벨",
                placeholder = "플레이스 홀더",
                value = normalValue,
                onValueChange = { normalValue = it },
                onClickClearButton = { normalValue = "" },
                helperText = "도움말 메세지",
            )

            SuwikiRegularTextField(
                label = "라벨",
                placeholder = "플레이스 홀더",
                value = errorValue,
                onValueChange = { errorValue = it },
                onClickClearButton = { errorValue = "" },
                helperText = "도움말 메세지",
                isError = true,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun SuwikiSmallTextFieldPreview() {
    SuwikiTheme {
        var normalValue by remember {
            mutableStateOf("")
        }

        Column(
            modifier = Modifier
                .background(White)
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SuwikiSmallTextField(
                placeholder = "플레이스 홀더",
                value = normalValue,
                onValueChange = { normalValue = it },
                onClickClearButton = { normalValue = "" },
            )

            SuwikiSmallTextField(
                placeholder = "플레이스 홀더",
                value = normalValue,
                onValueChange = { normalValue = it },
                onClickClearButton = { normalValue = "" },
            )
        }
    }
}