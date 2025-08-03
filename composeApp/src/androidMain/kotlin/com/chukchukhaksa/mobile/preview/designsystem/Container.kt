package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.component.container.SuwikiBoardContainer
import com.chukchukhaksa.mobile.common.designsystem.component.container.CchEditContainer
import com.chukchukhaksa.mobile.common.designsystem.component.container.CchSelectionButton
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme

@Composable
@Preview
fun SuwikiNoticeContainerPreview() {
    SuwikiTheme {
        Column {
            SuwikiBoardContainer(
                titleText = "Title",
                dateText = "date",
                onClick = {},
            )
        }
    }
}

@Composable
@Preview
fun TimetableEditContainerPreview() {
    SuwikiTheme {
        Column {
            CchEditContainer(
                name = "시간표시간표시간표시간표시간표시간표시간표시간표",
                semester = "1",
            )
        }
    }
}

@Preview
@Composable
fun SuwikiSelectionContainerPreview() {
    SuwikiTheme {
        CchSelectionButton(title = "title")
    }
}
