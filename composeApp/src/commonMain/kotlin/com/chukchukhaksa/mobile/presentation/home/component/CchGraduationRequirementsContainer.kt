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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_appbar_arrow_right_chukchuk
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable
import org.jetbrains.compose.resources.painterResource

@Composable
fun CchGraduationRequirementsContainer(
  major: String,
  gradeLevel: Int,
  totalEarnedCredits: Int,
  requiredCredits: Int,
  onClickGraduationProgress: () -> Unit
) {
  Column(
    modifier = Modifier
    .clip(RoundedCornerShape(16.dp))
    .background(White100)
    .border(1.dp, Gray200, RoundedCornerShape(16.dp))
    .padding(18.dp)
    .wrapContentHeight(),
  ) {
    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      text = major,
      color = Gray400,
      style = CchTheme.typography.bodySm,
      textAlign = TextAlign.Start
    )
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 18.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "${gradeLevel}학번 ${major} 졸업요건",
        color = Black100,
        style = CchTheme.typography.bodyLgStrong,
      )
      Image(
        modifier = Modifier
          .clip(CircleShape)
          .cchClickable(onClick = onClickGraduationProgress),
        painter = painterResource(resource = Res.drawable.ic_appbar_arrow_right_chukchuk),
        contentDescription = "",
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      CreaditStatusText(value = totalEarnedCredits)
      Text(
        text = "$totalEarnedCredits / $requiredCredits",
        color = Gray400,
        style = CchTheme.typography.bodySm,
      )
    }
  }
}

@Composable
fun CreaditStatusText(value: Int) {
  val annotatedString = buildAnnotatedString {
    append("총 ")

    withStyle(
      style = SpanStyle(
        color = Purple600,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp
      )
    ) {
      append(value.toString())
    }

    append(" 학점을 이수했어요 \uD83C\uDF89")
  }

  Text(
    text = annotatedString,
    color = Black100,
    style = CchTheme.typography.bodySm
  )
}
