package com.chukchukhaksa.mobile.presentation.home.graduationprogress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.CchAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray500
import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessListData
import com.chukchukhaksa.mobile.presentation.home.component.CchGraduationProgressContainer
import com.chukchukhaksa.mobile.presentation.home.component.CchSemesterGradeButton
import com.chukchukhaksa.mobile.presentation.home.component.CchTotalGradeContainer
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GraduationProgressRoute(
  popBackStack: () -> Unit = {},
  viewModel: GraduationProgressViewModel = koinViewModel(),
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.getGraduationProgress()
  }

  GraduationProgressScreen(
    graduationProgress = uiState.graduationProgress,
    onClickBackButton = popBackStack,
  )
}

@Composable
fun GraduationProgressScreen(
  graduationProgress: GraduationProcessListData,
  onClickBackButton: () -> Unit = {},
) {
  val scrollState = rememberScrollState()

  SuwikiBackground {
    Column {
      CchAppBarWithTitle(
        title = "18학번 정보통신학부 졸업요건",
        isShowAddButton = false,
        onClickBackButton = { onClickBackButton() },
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 11.dp, start = 20.dp, end = 20.dp)
          .verticalScroll(scrollState),
      ) {
        CchSemesterGradeButton(
          modifier = Modifier.padding(bottom = 26.dp),
          startSemester = "1학년 1학기",
          endSemester = "3학년 1학기",
          onClick = {},
        )
        Text(
          modifier = Modifier
            .align(alignment = Alignment.Start)
            .padding(bottom = 12.dp),
          text = "전체 수강내역",
          style = CchTheme.typography.bodyMdStrong,
          color = Gray500,
        )
        CchTotalGradeContainer(
          modifier = Modifier.padding(bottom = 19.dp),
          totalEarnedCredits = 109,
          cumulativeGpa = 3.03,
          percentile = 83.2,
        )
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          graduationProgress.graduationProgress.forEach { data ->
            CchGraduationProgressContainer(
              requirementStatus = data.requiredCredits <= data.earnedCredits,
              areaType = data.areaType,
              earnedCredits = data.earnedCredits,
              requiredCredits = data.requiredCredits,
              courses = data.courses,
            )
          }
        }
      }
    }
  }
}
