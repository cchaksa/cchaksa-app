package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_timetable_add
import chukchukhaksa.composeapp.generated.resources.ic_timetable_hamburger
import chukchukhaksa.composeapp.generated.resources.ic_timetable_setting
import chukchukhaksa.composeapp.generated.resources.word_timetable
import com.chukchukhaksa.mobile.common.designsystem.theme.Black
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.ui.suwikiClickable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TimetableAppbar(
    modifier: Modifier = Modifier,
    name: String? = null,
    onClickAdd: () -> Unit = {},
    onClickHamburger: () -> Unit = {},
    onClickSetting: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 28.dp, start = 24.dp, end = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = name,
            label = "name",
        ) { name ->
            Text(
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                text = name ?: stringResource(Res.string.word_timetable),
                style = SuwikiTheme.typography.header1,
                color = Black,
                maxLines = 1,
            )
        }

        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .suwikiClickable(onClick = onClickAdd),
            painter = painterResource(Res.drawable.ic_timetable_add),
            contentDescription = "",
            tint = Gray95,
        )

        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .suwikiClickable(onClick = onClickHamburger),
            painter = painterResource(Res.drawable.ic_timetable_hamburger),
            contentDescription = "",
            tint = Gray95,
        )

        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .suwikiClickable(onClick = onClickSetting),
            painter = painterResource(Res.drawable.ic_timetable_setting),
            contentDescription = "",
            tint = Gray95,
        )
    }
}
