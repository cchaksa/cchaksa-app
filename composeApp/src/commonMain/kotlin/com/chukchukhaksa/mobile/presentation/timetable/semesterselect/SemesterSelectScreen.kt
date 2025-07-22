package com.chukchukhaksa.mobile.presentation.timetable.semesterselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.ChukChukAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.button.ChukChukBasicButton
import com.chukchukhaksa.mobile.common.designsystem.component.container.ChukChukSelectionContainer
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SemesterSelectRoute(
    viewModel: SemesterSelectViewModel = koinViewModel(),
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->

    }
    SemesterSelectScreen(
        uiState = uiState,
        onClickSemester = viewModel::updateSelectedSemesterIndex
    )
}

@Composable
fun SemesterSelectScreen(
    uiState: SemesterSelectState = SemesterSelectState(),
    onClickSemester: (Int) -> Unit = {},
) {
    val semesters = semesterList.map { it.toText() }.toPersistentList()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChukChukAppBarWithTitle(
                title = "시간표 생성하기",
                onClickBackButton = { },
            )

            Text(
                modifier = Modifier.padding(top = 20.dp, bottom = 32.dp),
                text = "수강학기를 선택해주세요",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(letterSpacing = (-0.01).em)
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                semesters.forEachIndexed { idx, semester ->
                    Napier.i("idx == uiState.selectSemesterIndex: ${idx == uiState.selectSemesterIndex}")
                    ChukChukSelectionContainer(
                        modifier = Modifier.fillMaxWidth(),
                        text = semester,
                        isSelected = idx == uiState.selectSemesterIndex,
                        onClick = { onClickSemester(idx) }
                    )
                }
            }
        }
        ChukChukBasicButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
            text = "다음",
            enable = uiState.nextBtnEnable,
            onClick = {}
        )
    }
}
