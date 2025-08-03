package com.chukchukhaksa.mobile.common.designsystem.component.container

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.word_delete
import chukchukhaksa.composeapp.generated.resources.word_edit
import com.chukchukhaksa.mobile.common.designsystem.component.badge.CchBadge
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchSmallButton
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedSmallButton
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.Red400
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable
import org.jetbrains.compose.resources.stringResource

@Composable
fun CchEditContainer(
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
            .clip(RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = Gray200, shape = RoundedCornerShape(16.dp))
            .background(White100)
            .cchClickable(onClick = onClick)
            .padding(vertical = 21.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .weight(1f, false)
                .wrapContentHeight()
                .padding(end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f, false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = name,
                style = CchTheme.typography.bodyLgStrong,
                color = Black100,
            )

            CchBadge(text = semester)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CchSmallButton(
                text = stringResource(resource = Res.string.word_edit),
                textColor = Purple600,
                onClick = onClickEditButton,
            )
            CchSmallButton(
              text = stringResource(resource = Res.string.word_delete),
              textColor = Red400,
              onClick = onClickDeleteButton,
            )
        }
    }
}
