package com.chukchukhaksa.mobile.presentation.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_cch_default_profile
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.extension.toAcademicTerm
import org.jetbrains.compose.resources.painterResource

@Composable
fun CchProfileContainer(
  modifier: Modifier = Modifier,
  name: String,
  departmentName: String,
  studentCode: String,
  currentSemester: Int,
  status: String,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Image(
      painter = painterResource(resource = Res.drawable.ic_cch_default_profile),
      contentDescription = "",
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        modifier = Modifier.padding(bottom = 6.dp),
        text = name,
        style = CchTheme.typography.titleLg,
        color = Black100
      )
      Text(
        modifier = Modifier.padding(bottom = 5.dp),
        text = "$departmentName | $studentCode",
        style = CchTheme.typography.bodyMdStrong,
        color = Black100,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = currentSemester.toAcademicTerm(),
          style = CchTheme.typography.bodyMdStrong,
          color = Black100,
        )
        CchProfileStatusBadge(status)
      }
    }
  }
}

@Composable
fun CchProfileStatusBadge(
  status: String,
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(15.5.dp))
      .background(Color(0xffDAD6FE))
      .padding(vertical = 1.dp, horizontal = 10.dp)
  ) {
    Text(
      text = status,
      lineHeight = 1.6.em,
      letterSpacing = (-0.02).em,
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
      color = Purple600,
    )
  }
}
