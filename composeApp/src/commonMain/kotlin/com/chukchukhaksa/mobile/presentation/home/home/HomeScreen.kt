package com.chukchukhaksa.mobile.presentation.home.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.model.academic.AcademicSummary
import com.chukchukhaksa.mobile.common.model.profile.Profile
import com.chukchukhaksa.mobile.presentation.home.component.CchGraduationRequirementsContainer
import com.chukchukhaksa.mobile.presentation.home.component.CchProfileContainer
import com.chukchukhaksa.mobile.presentation.home.component.CchTotalGradeContainer

@Composable
fun HomeRoute(
  profile: Profile,
  academicSummary: AcademicSummary,
  onClickWebView: () -> Unit = {},
) {
  HomeScreen(
    profile = profile,
    academicSummary = academicSummary,
    onClickWebView = onClickWebView,
  )
}

@Composable
fun HomeScreen(
  profile: Profile,
  academicSummary: AcademicSummary,
  onClickWebView: () -> Unit = {},
) {
  Column(modifier = Modifier.padding(horizontal = 20.dp)) {
    CchProfileContainer(
      modifier = Modifier.padding(bottom = 15.dp),
      name = profile.name,
      departmentName = profile.departmentName,
      studentCode = profile.studentCode,
      currentSemester = profile.currentSemester,
      status = profile.status
    )
    CchTotalGradeContainer(
      modifier = Modifier.padding(bottom = 22.dp),
      totalEarnedCredits = academicSummary.totalEarnedCredits,
      cumulativeGpa = academicSummary.cumulativeGpa,
      percentile = academicSummary.percentile,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      CchGraduationRequirementsContainer(
        title = "주전공",
        major = profile.departmentName,
        gradeLevel = profile.gradeLevel,
        totalEarnedCredits = academicSummary.totalEarnedCredits,
        requiredCredits = academicSummary.requiredCredits,
        onClickGraduationProgress = onClickWebView,
      )
      if(profile.dualMajorName != "") {
        CchGraduationRequirementsContainer(
          title = "복수전공",
          major = profile.dualMajorName,
          gradeLevel = profile.gradeLevel,
          totalEarnedCredits = academicSummary.totalEarnedCredits,
          requiredCredits = academicSummary.requiredCredits,
          onClickGraduationProgress = onClickWebView,
        )
      }
    }
  }
}
