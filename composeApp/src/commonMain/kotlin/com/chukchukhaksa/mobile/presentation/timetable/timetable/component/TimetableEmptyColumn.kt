package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_cch_logo_ios
import chukchukhaksa.composeapp.generated.resources.ic_time_illust
import chukchukhaksa.composeapp.generated.resources.timetable_screen_create_timetable
import chukchukhaksa.composeapp.generated.resources.timetable_screen_create_timetable_button
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedMediumButton
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray600
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.timetableBorderWidth
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TimetableEmptyColumn(
  modifier: Modifier,
  onClickAdd: () -> Unit = {},
) {
  Column(
    modifier = modifier
      .padding(bottom = 10.dp, start = 20.dp, end = 20.dp)
      .fillMaxSize()
      .border(width = timetableBorderWidth, color = Gray200, shape = RoundedCornerShape(12.dp))
      .padding(horizontal = 71.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Image(
      modifier = Modifier
        .padding(top = 218.dp),
      painter = painterResource(Res.drawable.ic_time_illust),
      contentDescription = "cch logo",
    )

    Text(
      modifier = Modifier.padding(bottom = 20.dp),
      text = "아직 만들어진 시간표가 없어요!",
      color = Gray600,
      style = CchTheme.typography.bodyMd,
      textAlign = TextAlign.Center,
    )

    CchBasicButton(
      modifier = Modifier.padding(horizontal = 22.dp),
      text = "시간표 생성하기",
      enable = true,
      textStyle = CchTheme.typography.bodyMdStrong,
      onClick = { onClickAdd() }
    )
  }
}
