package com.chukchukhaksa.mobile.presentation.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_appbar_arrow_right_chukchuk
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable
import org.jetbrains.compose.resources.painterResource

@Composable
fun CchSemesterGradeButton(
  modifier: Modifier = Modifier,
  startSemester: String,
  endSemester: String,
  onClick: () -> Unit
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(White100)
      .border(1.dp, Gray200, RoundedCornerShape(16.dp))
      .padding(18.dp)
      .wrapContentHeight(),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "학기별 세부 성적 확인하기",
        color = Black100,
        style = CchTheme.typography.bodyLgStrong,
      )
      Image(
        modifier = Modifier
          .clip(CircleShape)
          .cchClickable(onClick = onClick),
        painter = painterResource(resource = Res.drawable.ic_appbar_arrow_right_chukchuk),
        contentDescription = "",
      )
    }
    Text(
      text = "$startSemester - $endSemester",
      color = Gray400,
      style = CchTheme.typography.bodySm
    )
  }
}
