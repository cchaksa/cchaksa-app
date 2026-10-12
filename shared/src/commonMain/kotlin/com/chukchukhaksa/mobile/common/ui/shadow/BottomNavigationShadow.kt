package com.chukchukhaksa.mobile.common.ui.shadow

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayDA

fun Modifier.bottomNavigationShadow(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 0.dp,
) = this.then(
    modifier.suwikiShadow(
        color = GrayDA,
        spread = 0.1.dp,
        borderRadius = borderRadius,
        blurRadius = 8.dp,
        offsetY = (-4).dp,
    ),
)
