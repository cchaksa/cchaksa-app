package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.SuwikiBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SuwikiBottomSheetPreview() {
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }

    // 테스트용 버튼
    Button(onClick = { isSheetOpen = true }) {
        Text("Bottom Sheet 열기")
    }

    SuwikiTheme {
        SuwikiBottomSheet(
            isSheetOpen = isSheetOpen,
            onDismissRequest = { isSheetOpen = !isSheetOpen },
            content = {},
        )
    }
}