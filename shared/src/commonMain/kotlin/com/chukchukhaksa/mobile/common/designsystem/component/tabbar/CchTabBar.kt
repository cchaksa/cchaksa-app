package com.chukchukhaksa.mobile.common.designsystem.component.tabbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.chukchukhaksa.mobile.common.kmp.isDebug
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.presentation.timetable.timetable.HomeTab
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HomeTabBar(
  // null이면 어떤 탭도 강조하지 않는다(초기 로드 중 탭 미결정 상태).
  selectedTab: HomeTab?,
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
      isSelected = (selectedTab == HomeTab.HOME),
      onClick = { onClickHome() }
    )

    TimetableTabBarItem(
      modifier = Modifier.padding(bottom = 2.dp),
      text = "시간표",
      isSelected = (selectedTab == HomeTab.EMPTY_TIMETABLE || selectedTab == HomeTab.TIMETABLE),
      // 디버그 빌드에서는 IDFA 진단용 3연타 제스처가 씹히지 않도록 클릭 쓰로틀을 해제한다.
      singleClick = !isDebug,
      onClick = { onClickTimetable() }
    )

    Spacer(modifier = Modifier.weight(1f))

    Icon(
      modifier = Modifier
        .padding(top = 4.dp, bottom = 4.dp, end = 10.dp)
        .clip(RoundedCornerShape(8.dp))
        .cchClickable { onClickMyPage() },
      painter = painterResource(Res.drawable.ic_tabbar_mypage),
      contentDescription = "",
      tint = Black100,
    )
  }
}

@Composable
fun TimetableTabBarItem(
  modifier: Modifier = Modifier,
  text: String,
  isSelected: Boolean,
  singleClick: Boolean = true,
  onClick: () -> Unit,
) {
  val textColor = if (isSelected) Black100 else Gray400

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .cchClickable(singleClick = singleClick) { onClick() },
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

@Preview
@Composable
private fun HomeTabBarPreview() {
  CchTheme {
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    HomeTabBar(
      selectedTab = selectedTab,
      onClickHome = { selectedTab = HomeTab.HOME },
      onClickTimetable = { selectedTab = HomeTab.TIMETABLE },
      onClickMyPage = {},
    )
  }
}
