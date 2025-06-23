package com.chukchukhaksa.mobile.common.designsystem.component.container

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.word_delete
import chukchukhaksa.composeapp.generated.resources.word_edit
import com.chukchukhaksa.mobile.common.designsystem.component.badge.BadgeColor
import com.chukchukhaksa.mobile.common.designsystem.component.badge.SuwikiBadge
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedSmallButton
import com.chukchukhaksa.mobile.common.designsystem.theme.Black
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.suwikiClickable
import org.jetbrains.compose.resources.stringResource

@Composable
fun SuwikiEditContainer(
    modifier: Modifier = Modifier,
    name: String,
    semester: String,
    onClickEditButton: () -> Unit = {},
    onClickDeleteButton: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = GrayF6,
                    start = Offset(0f, size.height - strokeWidth),
                    end = Offset(size.width, size.height - strokeWidth),
                    strokeWidth = strokeWidth,
                )
            }
            .suwikiClickable(onClick = onClick)
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .weight(1f, false)
                .wrapContentHeight()
                .padding(end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f, false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = name,
                style = SuwikiTheme.typography.header6,
                color = Black,
            )

            SuwikiBadge(color = BadgeColor.Gray, text = semester)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SuwikiContainedSmallButton(
                text = stringResource(resource = Res.string.word_edit),
                onClick = onClickEditButton,
            )
            SuwikiContainedSmallButton(
                text = stringResource(resource = Res.string.word_delete),
                onClick = onClickDeleteButton,
            )
        }
    }
}
