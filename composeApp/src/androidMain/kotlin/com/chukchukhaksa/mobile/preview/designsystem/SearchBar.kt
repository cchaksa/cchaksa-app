package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.component.searchbar.SuwikiSearchBar
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun SuwikiSearchBarPreview() {
    SuwikiTheme {
        var normalValue by remember {
            mutableStateOf("")
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SuwikiSearchBar(
                placeholder = "Hinted search text",
                value = normalValue,
                onValueChange = { normalValue = it },
                onClickClearButton = { normalValue = "" },
            )
        }
    }
}