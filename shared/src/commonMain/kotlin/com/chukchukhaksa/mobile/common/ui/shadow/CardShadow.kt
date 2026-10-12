package com.chukchukhaksa.mobile.common.ui.shadow

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayCB

fun Modifier.cardShadow(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 0.dp,
) = this.then(
    modifier.suwikiShadow(
        color = GrayCB,
        spread = 0.1.dp,
        borderRadius = borderRadius,
        blurRadius = 12.dp,
        offsetY = 8.dp,
        offsetX = 8.dp,
    ),
)
