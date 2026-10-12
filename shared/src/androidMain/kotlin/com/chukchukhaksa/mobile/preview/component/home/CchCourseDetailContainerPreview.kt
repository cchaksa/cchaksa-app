package com.chukchukhaksa.mobile.preview.component.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.model.academic.CourseDetail
import com.chukchukhaksa.mobile.presentation.home.component.CchCourseDetailContainer
import com.chukchukhaksa.mobile.presentation.home.component.CchTotalGradeContainer

@Preview
@Composable
fun CchCourseDetailContainerPreview() {
  val course = CourseDetail(
    areaType = "전공선택",
    courseCode = "09643",
    courseName = "모바일프로그래밍",
    credits = 3,
    grade = "B+",
    id = "0",
    isOnline = false,
    isRetake = false,
    isRetakeDelete = true,
    originalScore = 40,
    professor = "정원용",
    score = 94,
    semester = 10,
    year = 2023,
  )
  CchTheme {
    CchCourseDetailContainer(
      course = course
    )
  }
}
