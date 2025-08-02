package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
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
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CCHaksaTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.ui.cchClickable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TimetableAppbar(
  modifier: Modifier = Modifier,
  semester: String? = null,
  name: String? = null,
  onClickAdd: () -> Unit = {},
  onClickHamburger: () -> Unit = {},
  onClickSetting: () -> Unit = {},
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(2.dp),
    horizontalAlignment = Alignment.Start,
  ) {
    Text(
      text = semester ?: "",
      style = CCHaksaTheme.typography.bodySm,
      color = Purple600,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
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
          style = CCHaksaTheme.typography.bodyLgStrong,
          color = Black100,
          maxLines = 1,
        )
      }

      Icon(
        modifier = Modifier
          .clip(CircleShape)
          .cchClickable(onClick = onClickAdd),
        painter = painterResource(Res.drawable.ic_timetable_add),
        contentDescription = "",
        tint = Black100,
      )

      Icon(
        modifier = Modifier
          .clip(CircleShape)
          .cchClickable(onClick = onClickHamburger),
        painter = painterResource(Res.drawable.ic_timetable_hamburger),
        contentDescription = "",
        tint = Black100,
      )

      Icon(
        modifier = Modifier
          .clip(CircleShape)
          .cchClickable(onClick = onClickSetting),
        painter = painterResource(Res.drawable.ic_timetable_setting),
        contentDescription = "",
        tint = Black100,
      )
    }
  }
}
