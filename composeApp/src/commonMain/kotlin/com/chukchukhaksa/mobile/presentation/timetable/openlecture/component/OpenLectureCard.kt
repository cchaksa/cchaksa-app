package com.chukchukhaksa.mobile.presentation.timetable.openlecture.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.word_add
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedSmallButton
import com.chukchukhaksa.mobile.common.designsystem.theme.Black
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray100
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray500
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray6A
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayDA
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable
import org.jetbrains.compose.resources.stringResource

@Composable
fun OpenLectureCard(
  modifier: Modifier = Modifier,
  className: String,
  professor: String,
  cellInfo: String,
  grade: String,
  classType: String,
  openMajor: String,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(if (isSelected) Gray100 else White100)
      .drawBehind {
        val strokeWidth = 1.dp.toPx()
        drawLine(
          color = Gray100,
          start = Offset(0f, size.height - strokeWidth),
          end = Offset(size.width, size.height - strokeWidth),
          strokeWidth = strokeWidth,
        )
      }
      .cchClickable(
        onClick = onClick,
      )
      .padding(horizontal = 20.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          modifier = Modifier.weight(1f, fill = false),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          text = className,
          style = CchTheme.typography.bodySmStrong,
          color = Black100,
        )

        Text(
          maxLines = 1,
          text = professor,
          overflow = TextOverflow.Ellipsis,
          style = CchTheme.typography.bodyXs,
          color = Gray500,
        )
      }

      Text(
        text = cellInfo,
        style = CchTheme.typography.bodyXs,
        color = Gray500,
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = grade,
          style = CchTheme.typography.bodyXs,
          color = Gray500,
        )
        Text(
          text = classType,
          style = CchTheme.typography.bodyXs,
          color = Gray500,
        )
        Text(
          maxLines = 1,
          text = openMajor,
          style = CchTheme.typography.bodyXs,
          color = Gray500,
        )
      }
    }
  }
}

//@Preview
//@Composable
//fun OpenLectureCardPreview() {
//    SuwikiTheme {
//        Column {
//            OpenLectureCard(
//                modifier = Modifier,
//                className = "강의명 강의명 강의명 강의명 강의명 강의명 강의명 강의명 강의명 강의명 강의명",
//                professor = "교수명 교수명 교수명 교수명 교수명",
//                cellInfo = "목 6,7교시 (미래211) 목 6,7교시 (미래211) 목 6,7교시 (미래211) 목 6,7교시 (미래211)",
//                grade = "학년",
//                classType = "강의유형",
//                openMajor = "개설학과 개설학과 개설학과 개설학과 개설학과 개설학과",
//                onClick = {},
//                onClickAdd = {},
//            )
//        }
//    }
//}
