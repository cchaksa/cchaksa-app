package com.chukchukhaksa.mobile.preview.component.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessCourse
import com.chukchukhaksa.mobile.presentation.home.component.CchGraduationProgressContainer
import com.chukchukhaksa.mobile.presentation.home.component.CchGraduationRequirementsContainer

//@Preview(heightDp = 131)
@Preview
@Composable
fun CchGraduationProgressContainerPreview() {
  val courses = listOf(
    GraduationProcessCourse(
      courseName = "회로이론",
      credits = 2,
      grade = "A+",
      semester = 10,
      year = 2023,
    ),
    GraduationProcessCourse(
      courseName = "회로이론",
      credits = 2,
      grade = "B+",
      semester = 10,
      year = 2023,
    ),
    GraduationProcessCourse(
      courseName = "회로이론",
      credits = 2,
      grade = "C+",
      semester = 10,
      year = 2023,
    ),
    GraduationProcessCourse(
      courseName = "회로이론",
      credits = 2,
      grade = "D+",
      semester = 10,
      year = 2023,
    ),
    GraduationProcessCourse(
      courseName = "회로이론",
      credits = 2,
      grade = "F",
      semester = 10,
      year = 2023,
    ),
  )
  CchTheme {
    CchGraduationProgressContainer(
      requirementStatus = true,
      areaType = "중요핵심",
      earnedCredits = 5,
      requiredCredits = 6,
      courses = courses,
    )
  }
}
