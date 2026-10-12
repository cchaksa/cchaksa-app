package com.chukchukhaksa.mobile.presentation.home.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_arrow_down_black
import chukchukhaksa.composeapp.generated.resources.ic_arrow_down_white
import chukchukhaksa.composeapp.generated.resources.ic_check_box
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple300
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple500
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessCourse
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.common.ui.gradeColor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun CchGraduationProgressContainer(
  requirementStatus: Boolean,
  areaType: String,
  earnedCredits: Int,
  requiredCredits: Int,
  courses: List<GraduationProcessCourse>,
) {
  val isExpanded = remember { mutableStateOf(false) }
  val backgroundColor = if (requirementStatus) Black100 else White100
  val areaTypeTextColor = if (requirementStatus) Purple500 else Black100
  val creditTextColor = if (requirementStatus) White100 else Gray400
  val courseTextColor = if (requirementStatus) White100 else Black100
  val courseCreditTextColor = if (requirementStatus) Purple300 else Purple600
  val arrowIcon: DrawableResource = if (requirementStatus) Res.drawable.ic_arrow_down_white else Res.drawable.ic_arrow_down_black

  val rotationAngle by animateFloatAsState(
    targetValue = if (isExpanded.value) 180f else 0f,
    label = "Rotation"
  )

  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(backgroundColor)
      .animateContentSize(
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioLowBouncy,
          stiffness = Spring.StiffnessLow
        )
      )
      .wrapContentHeight(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .cchClickable(onClick = { isExpanded.value = !isExpanded.value })
        .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = if(isExpanded.value) 14.dp else 20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = areaType,
          color = areaTypeTextColor,
          style = CchTheme.typography.bodyLgStrong,
        )
        Text(
          text = "$earnedCredits / $requiredCredits",
          color = creditTextColor,
          style = CchTheme.typography.bodyMdStrong,
        )
        if (requirementStatus) {
          Image(
            painter = painterResource(resource = Res.drawable.ic_check_box),
            contentDescription = "",
          )
        }
      }
      Image(
        modifier = Modifier.rotate(rotationAngle),
        painter = painterResource(resource = arrowIcon),
        contentDescription = "",
      )
    }
    if (isExpanded.value) {
      Column(
        modifier = Modifier.padding(bottom = 18.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        courses.forEach {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = it.courseName,
                color = courseTextColor,
                style = CchTheme.typography.bodyMdStrong,
              )
              Text(
                text = "${it.year}-${it.semester}",
                color = Gray400,
                style = CchTheme.typography.bodySm,
              )
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "${it.credits}학점",
                color = courseCreditTextColor,
                style = CchTheme.typography.bodySm,
              )
              Text(
                text = it.grade,
                color = it.grade.gradeColor(),
                style = CchTheme.typography.bodySm,
              )
            }
          }
        }
      }
    }
  }

}
