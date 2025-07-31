package com.chukchukhaksa.mobile.preview.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.layout.Column
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.presentation.timetable.openlecture.component.OpenLectureCard

@Preview
@Composable
fun OpenLectureCardPreview() {
  CchTheme {
    OpenLectureCard(
      modifier = Modifier,
      className = "강의명 강의명 강의명 강의명 강의명 강의명 강의명 강의명 강의명 강의명 강의명",
      professor = "교수명 교수명 교수명 교수명 교수명",
      cellInfo = "목 6,7교시 (미래211) 목 6,7교시 (미래211) 목 6,7교시 (미래211) 목 6,7교시 (미래211)",
      grade = "학년",
      classType = "강의유형",
      openMajor = "개설학과 개설학과 개설학과 개설학과 개설학과 개설학과",
      isSelected = true,
      onClick = {}
    )
  }
}
