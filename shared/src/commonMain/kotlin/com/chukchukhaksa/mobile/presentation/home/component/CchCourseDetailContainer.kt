package com.chukchukhaksa.mobile.presentation.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Red400
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.model.academic.CourseDetail
import com.chukchukhaksa.mobile.common.ui.gradeColor

@Composable
fun CchCourseDetailContainer(
  course: CourseDetail
) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(White100)
      .border(1.dp, Gray200, RoundedCornerShape(16.dp))
      .padding(top = 18.dp, bottom = 20.dp, start = 18.dp, end = 18.dp)
      .wrapContentHeight(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 17.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "${course.courseCode} | ${course.areaType} | ${course.credits}학점",
        color = Gray400,
        style = CchTheme.typography.bodySm,
      )
      Text(
        text = "${course.professor} 교수",
        color = Gray400,
        style = CchTheme.typography.bodySm,
      )
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = course.courseName,
        color = Black100,
        style = CchTheme.typography.bodyLgStrong,
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = course.grade,
          color = course.grade.gradeColor(),
          style = CchTheme.typography.bodyLgStrong,
        )
        Text(
          text = "${course.score}",
          color = Black100,
          style = CchTheme.typography.bodySm,
        )
      }
    }
    if(course.isRetakeDelete) {
      Text(
        text = "재수강삭제",
        color = Red400,
        style = CchTheme.typography.bodySm,
      )
    }
  }
}
