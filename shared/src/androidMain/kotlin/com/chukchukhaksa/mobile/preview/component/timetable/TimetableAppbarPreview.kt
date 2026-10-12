package com.chukchukhaksa.mobile.preview.component.timetable

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.TimetableAppbar

@Preview
@Composable
fun TimetableAppbarPreview() {
    SuwikiTheme {
        TimetableAppbar()
    }
}