package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.component.badge.BadgeColor
import com.chukchukhaksa.mobile.common.designsystem.component.badge.SuwikiBadge
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme

@Preview
@Composable
fun BadgePreview() {
    SuwikiTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SuwikiBadge(text = "label", color = BadgeColor.Blue)
            SuwikiBadge(text = "label", color = BadgeColor.Gray)
        }
    }
}