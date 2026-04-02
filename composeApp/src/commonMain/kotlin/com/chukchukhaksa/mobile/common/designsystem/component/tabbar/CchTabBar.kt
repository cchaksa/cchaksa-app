package com.chukchukhaksa.mobile.common.designsystem.component.tabbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_tabbar_dot
import chukchukhaksa.composeapp.generated.resources.ic_tabbar_mypage
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.presentation.timetable.timetable.TimetableScreen
import org.jetbrains.compose.resources.painterResource

@Composable
fun TimetableTabBar(
  timetableScreenContent: TimetableScreen,
  onClickHome: () -> Unit,
  onClickTimetable: () -> Unit,
  onClickMyPage: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(10.dp)
  ) {
    TimetableTabBarItem(
      modifier = Modifier.padding(start = 14.dp, end = 24.dp, bottom = 2.dp),
      text = "홈",
      isSelected = (timetableScreenContent == TimetableScreen.HOME),
      onClick = { onClickHome() }
    )

    TimetableTabBarItem(
      modifier = Modifier.padding(bottom = 2.dp),
      text = "시간표",
      isSelected = (timetableScreenContent == TimetableScreen.EMPTY_TIMETABLE || timetableScreenContent == TimetableScreen.TIMETABLE),
      onClick = { onClickTimetable() }
    )

    Spacer(modifier = Modifier.weight(1f))

//    Icon(
//      modifier = Modifier
//        .padding(top = 4.dp, bottom = 4.dp, end = 10.dp)
//        .cchClickable { onClickMyPage() },
//      painter = painterResource(Res.drawable.ic_tabbar_mypage),
//      contentDescription = "",
//      tint = Black100,
//    )
  }
}

@Composable
fun TimetableTabBarItem(
  modifier: Modifier = Modifier,
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val textColor = if (isSelected) Black100 else Gray400

  Column(
    modifier = modifier
      .clip(CircleShape)
      .cchClickable { onClick() },
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      painter = painterResource(Res.drawable.ic_tabbar_dot),
      contentDescription = "",
      tint = if (isSelected) Black100 else White100,
    )
    Text(
      text = text,
      style = CchTheme.typography.bodyMdStrong,
      color = textColor,
    )
  }
}
