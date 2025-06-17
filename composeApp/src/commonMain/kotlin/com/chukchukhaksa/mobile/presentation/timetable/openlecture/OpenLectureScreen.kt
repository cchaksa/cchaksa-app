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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.add_timetable_cell_search_bar_placeholder
import chukchukhaksa.composeapp.generated.resources.add_timetable_screen_add_lecture
import chukchukhaksa.composeapp.generated.resources.ic_align_checked
import chukchukhaksa.composeapp.generated.resources.ic_dropdown_arrow_down
import chukchukhaksa.composeapp.generated.resources.open_lecture_screen_empty_result_description
import chukchukhaksa.composeapp.generated.resources.open_lecture_screen_empty_result_title
import chukchukhaksa.composeapp.generated.resources.open_lecture_success_add_cell_toast
import chukchukhaksa.composeapp.generated.resources.word_apply
import chukchukhaksa.composeapp.generated.resources.word_num_school_level
import chukchukhaksa.composeapp.generated.resources.word_open_major
import chukchukhaksa.composeapp.generated.resources.word_school_level
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.SuwikiAppBarWithTextButton
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.SuwikiBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.SuwikiSelectBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedLargeButton
import com.chukchukhaksa.mobile.common.designsystem.component.loading.LoadingScreen
import com.chukchukhaksa.mobile.common.designsystem.component.searchbar.SuwikiSearchBar
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray6A
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.designsystem.theme.Primary
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.model.OpenLecture
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.common.ui.suwikiClickable
import com.chukchukhaksa.mobile.common.ui.timetableCellColorHexMap
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
    selectedOpenMajor: String,
    popBackStack: () -> Unit,
    handleException: (Throwable) -> Unit,
    onShowToast: (String) -> Unit,
    navigateOpenMajor: (String) -> Unit,
    navigateCellEditor: (CellEditorArgument) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is OpenLectureSideEffect.HandleException -> handleException(sideEffect.throwable)
            OpenLectureSideEffect.NavigateAddCustomTimetableCell -> navigateCellEditor(
                CellEditorArgument()
            )

            is OpenLectureSideEffect.NavigateOpenMajor -> navigateOpenMajor(sideEffect.selectedOpenMajor)
            OpenLectureSideEffect.PopBackStack -> popBackStack()
            OpenLectureSideEffect.ScrollToTop -> scope.launch {
//                awaitFrame()
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
        onClickOpenMajorFilterContainer = navigateOpenMajor,
        onDismissSchoolLevelBottomSheet = viewModel::hideGradeBottomSheet,
        onClickSchoolLevelFilterContainer = viewModel::showGradeBottomSheet,
        onClickSchoolLevelBottomSheetItem = { position ->
            viewModel.hideGradeBottomSheet()
            viewModel.updateSchoolLevelPosition(SchoolLevel.entries[position])
        },
        onClickBack = viewModel::popBackStack,
        onClickSearchButton = viewModel::searchOpenLecture,
        onClickClearButton = { viewModel.updateSearchValue("") },
        onValueChangeSearch = viewModel::updateSearchValue,
        onClickCellAdd = viewModel::showSelectColorBottomSheet,
        onClickApply = {
            viewModel.hideSelectColorBottomSheet()
            viewModel.insertTimetable()
        },
        onClickColorChip = viewModel::updateSelectedCellColor,
        onDismissColorSelectBottomSheet = viewModel::hideSelectColorBottomSheet,
        onClickClassInfoCard = viewModel::navigateCellEditor,
        onClickCustomAdd = viewModel::navigateAddCustomCell,
    )
}

@Composable
fun OpenLectureScreen(
    uiState: OpenLectureState = OpenLectureState(),
    listState: LazyListState = rememberLazyListState(),
    onClickOpenMajorFilterContainer: (String) -> Unit = {},
    onDismissSchoolLevelBottomSheet: () -> Unit = {},
    onClickSchoolLevelFilterContainer: () -> Unit = {},
    onClickSchoolLevelBottomSheetItem: (Int) -> Unit = {},
    onClickBack: () -> Unit = {},
    onClickSearchButton: (String) -> Unit = {},
    onClickClearButton: () -> Unit = {},
    onValueChangeSearch: (String) -> Unit = {},
    onClickCellAdd: (OpenLecture) -> Unit = {},
    onClickApply: () -> Unit = {},
    onDismissColorSelectBottomSheet: () -> Unit = {},
    onClickColorChip: (TimetableCellColor) -> Unit = {},
    onClickClassInfoCard: (OpenLecture) -> Unit = {},
    onClickCustomAdd: () -> Unit = {},
) {

    SuwikiBackground {
        // TODO Collapsing toolbar 적용 필요
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
        ) {
            SuwikiAppBarWithTextButton(
                buttonText = stringResource(Res.string.add_timetable_screen_add_lecture),
                onClickBack = onClickBack,
                onClickTextButton = onClickCustomAdd,
            )

            Column(
                modifier = Modifier
                    .weight(1f),
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        FilterContainer(
                            filterName = stringResource(Res.string.word_open_major),
                            value = uiState.selectedOpenMajor,
                            onClick = { onClickOpenMajorFilterContainer(uiState.selectedOpenMajor) },
                        )

                        FilterContainer(
                            filterName = stringResource(Res.string.word_school_level),
                            value = stringResource(uiState.schoolLevel.stringResId),
                            onClick = onClickSchoolLevelFilterContainer,
                        )
                    }

                    SuwikiSearchBar(
                        modifier = Modifier.padding(top = 10.dp),
                        placeholder = stringResource(Res.string.add_timetable_cell_search_bar_placeholder),
                        onClickSearchButton = onClickSearchButton,
                        value = uiState.searchValue,
                        onClickClearButton = onClickClearButton,
                        onValueChange = onValueChangeSearch,
                    )

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
                    ) {
                        Spacer(modifier = Modifier.size(52.dp))

                        Text(
                            text = stringResource(Res.string.open_lecture_screen_empty_result_title),
                            style = SuwikiTheme.typography.header4,
                            color = Gray95,
                        )

                        Spacer(modifier = Modifier.size(12.dp))

                        Text(
                            text = stringResource(Res.string.open_lecture_screen_empty_result_description),
                            style = SuwikiTheme.typography.body5,
                            textAlign = TextAlign.Center,
                            color = Gray95,
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
                                onClick = { onClickClassInfoCard(this) },
                                onClickAdd = { onClickCellAdd(this) },
                            )
                        }
                    }
                }
            }
        }
    }

    ColorSelectBottomSheet(
        isSheetOpen = uiState.showSelectCellColorBottomSheet,
        selectedTimetableCellColor = uiState.selectedTimetableCellColor,
        onClickApply = onClickApply,
        onClickColorChip = onClickColorChip,
        onDismissColorSelectBottomSheet = onDismissColorSelectBottomSheet,
    )

    if (uiState.isLoading) {
        LoadingScreen()
    }

    SuwikiSelectBottomSheet(
        isSheetOpen = uiState.showSchoolLevelBottomSheet,
        onDismissRequest = onDismissSchoolLevelBottomSheet,
        onClickItem = onClickSchoolLevelBottomSheetItem,
        itemList = SchoolLevel.entries.map { stringResource(it.stringResId) }
            .toPersistentList(),
        title = stringResource(Res.string.word_school_level),
        selectedPosition = uiState.schoolLevel.ordinal,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSelectBottomSheet(
    isSheetOpen: Boolean,
    onDismissColorSelectBottomSheet: () -> Unit,
    selectedTimetableCellColor: TimetableCellColor,
    onClickColorChip: (TimetableCellColor) -> Unit,
    onClickApply: () -> Unit,
) {
    SuwikiBottomSheet(
        isSheetOpen = isSheetOpen,
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
                            .suwikiClickable(
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
    filterName: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(size = 10.dp))
            .border(width = 1.dp, color = GrayF6, shape = RoundedCornerShape(size = 10.dp))
            .suwikiClickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = filterName, style = SuwikiTheme.typography.body6, color = Gray6A)
        Spacer(modifier = Modifier.size(4.dp))
        Text(text = value, style = SuwikiTheme.typography.body6, color = Primary)
        Icon(
            painter = painterResource(Res.drawable.ic_dropdown_arrow_down),
            contentDescription = null,
            tint = Gray6A,
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
