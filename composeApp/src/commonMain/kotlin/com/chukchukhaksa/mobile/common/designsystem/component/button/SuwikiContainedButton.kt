package com.chukchukhaksa.mobile.common.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Black
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.designsystem.theme.Primary
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White

@Composable
fun SuwikiContainedLargeButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(15.dp),
    text: String,
    enabled: Boolean = true,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
) {
    BasicButton(
        modifier = modifier
            .fillMaxWidth(),
        text = text,
        shape = shape,
        onClick = onClick,
        backgroundColor = if (enabled) Primary else GrayF6,
        rippleColor = Black,
        textColor = if (enabled) White else Gray95,
        textStyle = SuwikiTheme.typography.header5,
        clickable = clickable,
        padding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    )
}

@Composable
fun SuwikiContainedMediumButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    text: String,
    enabled: Boolean = true,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
) {
    BasicButton(
        modifier = modifier,
        text = text,
        shape = shape,
        onClick = onClick,
        backgroundColor = if (enabled) Primary else GrayF6,
        rippleColor = Black,
        textColor = if (enabled) White else Gray95,
        textStyle = SuwikiTheme.typography.body2,
        clickable = clickable,
        padding = PaddingValues(horizontal = 16.dp, vertical = 10.5.dp),
    )
}

@Composable
fun SuwikiContainedSmallButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(5.dp),
    text: String,
    onClick: () -> Unit = {},
) {
    BasicButton(
        modifier = modifier,
        text = text,
        shape = shape,
        onClick = onClick,
        backgroundColor = GrayF6,
        rippleColor = Black,
        textColor = Gray95,
        textStyle = SuwikiTheme.typography.caption2,
        padding = PaddingValues(horizontal = 6.dp, vertical = 2.5.dp),
    )
}
