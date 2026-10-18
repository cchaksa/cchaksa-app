package com.chukchukhaksa.mobile.presentation.portalscrape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable

/** 포털 스크래핑 테스트 화면으로 들어가는 버튼. 호출하는 쪽에서 디버그 빌드일 때만 노출한다. */
@Composable
fun PortalScrapeTestButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Text(
    modifier = modifier
      .clip(RoundedCornerShape(100.dp))
      .background(Black100)
      .cchClickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 10.dp),
    text = "포털 스크래핑 테스트",
    style = CchTheme.typography.bodySmStrong,
    color = White100,
  )
}
