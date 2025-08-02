package com.chukchukhaksa.mobile.presentation.timetable.openlecture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.add_timetable_cell_search_bar_placeholder
import chukchukhaksa.composeapp.generated.resources.ic_align_checked
import chukchukhaksa.composeapp.generated.resources.ic_arrow
import chukchukhaksa.composeapp.generated.resources.ic_arrow_sm
import chukchukhaksa.composeapp.generated.resources.ic_plus_s
import chukchukhaksa.composeapp.generated.resources.open_lecture_screen_empty_result_title
import chukchukhaksa.composeapp.generated.resources.open_lecture_success_add_cell_toast
import chukchukhaksa.composeapp.generated.resources.word_apply
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.CchBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.CchSelectBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedLargeButton
import com.chukchukhaksa.mobile.common.designsystem.component.loading.LoadingScreen
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.CchSearchTextField
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray600
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple100
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.model.OpenLecture
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.common.ui.runIf
import com.chukchukhaksa.mobile.common.ui.timetableCellColorHexMap
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor.OpenMajorBottomSheet
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.openlecture.component.OpenLectureCard
import com.chukchukhaksa.mobile.presentation.timetable.openlecture.model.SchoolLevel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OpenLectureRoute(
  viewModel: OpenLectureViewModel = koinViewModel(),
  selectedOpenMajor: String?,
  popBackStack: () -> Unit,
  handleException: (Throwable) -> Unit,
  onShowToast: (String) -> Unit,
  navigateCellEditor: (CellEditorArgument) -> Unit,
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
    when (sideEffect) {
      is OpenLectureSideEffect.HandleException -> handleException(sideEffect.throwable)
      OpenLectureSideEffect.NavigateAddCustomTimetableCell -> navigateCellEditor(
        CellEditorArgument(),
      )

      OpenLectureSideEffect.PopBackStack -> popBackStack()
      OpenLectureSideEffect.ScrollToTop -> scope.launch {
        listState.animateScrollToItem(0)
      }

      is OpenLectureSideEffect.ShowOverlapCellToast -> onShowToast(sideEffect.msg)
      OpenLectureSideEffect.ShowSuccessAddCellToast -> onShowToast(getString(Res.string.open_lecture_success_add_cell_toast))
      is OpenLectureSideEffect.NavigateCellEditor -> navigateCellEditor(sideEffect.argument)
    }
  }

  LaunchedEffect(selectedOpenMajor) {
    viewModel.updateSelectedOpenMajor(selectedOpenMajor)
  }

  LaunchedEffect(key1 = viewModel) {
    viewModel.initData()
  }

  OpenLectureScreen(
    uiState = uiState,
    listState = listState,
    onClickOpenMajorFilterContainer = { viewModel.showOpenMajorBottomSheet() },
    onDismissSchoolLevelBottomSheet = viewModel::hideGradeBottomSheet,
    onClickSchoolLevelFilterContainer = viewModel::showGradeBottomSheet,
    onClickSchoolLevelBottomSheetItem = { position ->
      viewModel.hideGradeBottomSheet()
      viewModel.updateSchoolLevelPosition(SchoolLevel.entries[position])
    },
    onClickBack = viewModel::popBackStack,
    onClickSearchButton = viewModel::searchOpenLecture,
    onValueChangeSearch = viewModel::updateSearchValue,
    onClickCellAdd = viewModel::showSelectColorBottomSheet,
    onSelectOpenLecture = viewModel::selectOpenLecture,
    onClickAddSelectedLecture = viewModel::insertSelectedLectureToTimetable,
    onClickApply = {
      viewModel.hideSelectColorBottomSheet()
      viewModel.insertTimetable()
    },
    onClickColorChip = viewModel::updateSelectedCellColor,
    onDismissColorSelectBottomSheet = viewModel::hideSelectColorBottomSheet,
    onClickClassInfoCard = viewModel::navigateCellEditor,
    onClickCustomAdd = viewModel::navigateAddCustomCell,
    onDismissOpenMajorBottomSheet = viewModel::hideOpenMajorBottomSheet,
    onConfirmOpenMajor = viewModel::confirmOpenMajor,
    handleException = handleException,
    onShowToast = onShowToast,
  )
}

@Composable
fun OpenLectureScreen(
  uiState: OpenLectureState = OpenLectureState(),
  listState: LazyListState = rememberLazyListState(),
  onClickOpenMajorFilterContainer: () -> Unit = {},
  onDismissSchoolLevelBottomSheet: () -> Unit = {},
  onClickSchoolLevelFilterContainer: () -> Unit = {},
  onClickSchoolLevelBottomSheetItem: (Int) -> Unit = {},
  onClickBack: () -> Unit = {},
  onClickSearchButton: (String) -> Unit = {},
  onValueChangeSearch: (String) -> Unit = {},
  onClickCellAdd: (OpenLecture) -> Unit = {},
  onSelectOpenLecture: (Long) -> Unit = {},
  onClickApply: () -> Unit = {},
  onDismissColorSelectBottomSheet: () -> Unit = {},
  onClickColorChip: (TimetableCellColor) -> Unit = {},
  onClickClassInfoCard: (OpenLecture) -> Unit = {},
  onClickCustomAdd: () -> Unit = {},
  onDismissOpenMajorBottomSheet: () -> Unit = {},
  onConfirmOpenMajor: (String?) -> Unit = {},
  handleException: (Throwable) -> Unit = {},
  onShowToast: (String) -> Unit = {},
  onClickAddSelectedLecture: () -> Unit = {},
) {
  SuwikiBackground {
    Box {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(White),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, top = 11.dp, bottom = 14.dp, end = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            modifier = Modifier
              .clip(CircleShape)
              .cchClickable(onClick = onClickBack),
            painter = painterResource(resource = Res.drawable.ic_arrow),
            tint = Black100,
            contentDescription = "뒤로가기",
          )

          Spacer(Modifier.width(2.dp))

          Text(
            text = "강의추가",
            style = CchTheme.typography.bodyLgStrong,
          )
        }

        Column(
          modifier = Modifier
            .weight(1f),
        ) {
          Column {
            CchSearchTextField(
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
              placeholder = stringResource(Res.string.add_timetable_cell_search_bar_placeholder),
              value = uiState.searchValue,
              onValueChange = onValueChangeSearch,
              onSearchAction = { onClickSearchButton(uiState.searchValue) },
            )

            Row(
              modifier = Modifier.padding(horizontal = 20.dp),
            ) {
              val openMajorFiltered = uiState.selectedOpenMajor != null
              val schoolLevelFiltered = uiState.schoolLevel != SchoolLevel.ALL
              FilterContainer(
                value = uiState.selectedOpenMajor ?: "전체 학과",
                onClick = onClickOpenMajorFilterContainer,
                isSelected = openMajorFiltered,
              )

              Spacer(Modifier.width(8.dp))

              FilterContainer(
                value = if (schoolLevelFiltered.not()) "전체 학년" else stringResource(uiState.schoolLevel.stringResId),
                onClick = onClickSchoolLevelFilterContainer,
                isSelected = schoolLevelFiltered,
              )

              Spacer(Modifier.weight(1f))

              CustomAddButton(
                onClick = onClickCustomAdd,
              )
            }

            Text(
              modifier = Modifier
                .padding(top = 10.dp, end = 20.dp)
                .align(Alignment.End),
              text = "최근 갱신일: ${uiState.lastUpdatedDate ?: "확인 중"}",
              style = SuwikiTheme.typography.body7,
              color = Gray95,
            )
          }
          if (uiState.openLectureList.isEmpty() && uiState.isLoading.not()) {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
            ) {
              Text(
                text = stringResource(Res.string.open_lecture_screen_empty_result_title),
                style = CchTheme.typography.bodyMd,
                color = Gray600,
              )
            }
          }

          LazyColumn(
            modifier = Modifier
              .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            state = listState,
          ) {
            items(
              items = uiState.openLectureList,
              key = { it.id },
            ) { lectureEvaluation ->
              with(lectureEvaluation) {
                OpenLectureCard(
                  className = name,
                  professor = professorName,
                  cellInfo = originalCellList.toText(),
                  grade = "${grade}학년",
                  classType = type,
                  openMajor = major,
                  onClick = { onSelectOpenLecture(this.id) },
                  isSelected = uiState.selectedOpenLectureId == this.id,
//                onClickAdd = { onClickCellAdd(this) },
                )
              }
            }
          }
        }
      }

      CchBasicButton(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(start = 16.dp, end = 16.dp, bottom = 2.dp),
        text = "선택한 강의 추가하기",
        enable = uiState.selectedOpenLectureId != null,
        onClick = onClickAddSelectedLecture,
      )
    }
  }

  if (uiState.showSelectCellColorBottomSheet) {
    ColorSelectBottomSheet(
      selectedTimetableCellColor = uiState.selectedTimetableCellColor,
      onClickApply = onClickApply,
      onClickColorChip = onClickColorChip,
      onDismissColorSelectBottomSheet = onDismissColorSelectBottomSheet,
    )
  }

  if (uiState.isLoading) {
    LoadingScreen()
  }

  if (uiState.showSchoolLevelBottomSheet) {
    CchSelectBottomSheet(
      onDismissRequest = onDismissSchoolLevelBottomSheet,
      onClickItem = onClickSchoolLevelBottomSheetItem,
      itemList = SchoolLevel.entries.map { stringResource(it.stringResId) }
        .toPersistentList(),
      selectedPosition = uiState.schoolLevel.ordinal,
    )
  }


  if (uiState.showOpenMajorBottomSheet) {
    OpenMajorBottomSheet(
      selectedOpenMajor = uiState.selectedOpenMajor,
      onDismissRequest = onDismissOpenMajorBottomSheet,
      onConfirm = onConfirmOpenMajor,
      handleException = handleException,
      onShowToast = onShowToast,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSelectBottomSheet(
  onDismissColorSelectBottomSheet: () -> Unit,
  selectedTimetableCellColor: TimetableCellColor,
  onClickColorChip: (TimetableCellColor) -> Unit,
  onClickApply: () -> Unit,
) {
  CchBottomSheet(
    onDismissRequest = onDismissColorSelectBottomSheet,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
      verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
      LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        items(items = TimetableCellColor.entries) {
          Box(
            modifier = Modifier
              .aspectRatio(1f)
              .clip(CircleShape)
              .background(Color(timetableCellColorHexMap[it]!!))
              .cchClickable(
                rippleEnabled = false,
                onClick = { onClickColorChip(it) },
              ),
            contentAlignment = Alignment.Center,
          ) {
            if (selectedTimetableCellColor == it) {
              Icon(
                painter = painterResource(Res.drawable.ic_align_checked),
                contentDescription = null,
                tint = White,
              )
            }
          }
        }
      }

      SuwikiContainedLargeButton(
        text = stringResource(Res.string.word_apply),
        onClick = onClickApply,
      )
    }
  }
}

@Composable
private fun FilterContainer(
  value: String,
  onClick: () -> Unit,
  isSelected: Boolean = false,
) {
  val backgroundColor = if (isSelected) Purple100 else White100
  val textColor = if (isSelected) Purple600 else Gray600
  val iconColor = if (isSelected) Purple600 else Gray600

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(size = 6.dp))
      .background(backgroundColor)
      .cchClickable(onClick = onClick)
      .runIf(isSelected.not()) { border(width = 1.dp, color = Gray200, shape = RoundedCornerShape(size = 6.dp)) }
      .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = value, style = CchTheme.typography.bodySm, color = textColor)
    Spacer(Modifier.width(2.dp))
    Icon(
      painter = painterResource(Res.drawable.ic_arrow_sm),
      contentDescription = null,
      tint = iconColor,
    )
  }
}

@Composable
private fun CustomAddButton(
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(Black100)
      .cchClickable(onClick = onClick, rippleColor = White100)
      .padding(start = 6.dp, top = 7.dp, bottom = 6.dp, end = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = painterResource(Res.drawable.ic_plus_s),
      contentDescription = null,
      tint = White100,
    )

    Text(
      text = "직접 추가",
      style = CchTheme.typography.bodySm,
      color = White100,
    )
  }
}

//@Preview
//@Composable
//fun OpenLectureScreenPreview() {
//    SuwikiTheme {
//        OpenLectureScreen()
//    }
//}
