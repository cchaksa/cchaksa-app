package com.chukchukhaksa.mobile.presentation.timetable.celleditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_color
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_input_lecture_name
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_input_location
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_input_professor_name
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_lecture_name
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_need_lecture_name
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_need_location
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_need_professor_name
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_period
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_professor_name
import chukchukhaksa.composeapp.generated.resources.add_cell_screen_time_location
import chukchukhaksa.composeapp.generated.resources.ic_align_checked
import chukchukhaksa.composeapp.generated.resources.ic_arrow
import chukchukhaksa.composeapp.generated.resources.ic_close
import chukchukhaksa.composeapp.generated.resources.open_lecture_success_add_cell_toast
import chukchukhaksa.composeapp.generated.resources.open_lecture_success_edit_cell_toast
import chukchukhaksa.composeapp.generated.resources.word_add
import chukchukhaksa.composeapp.generated.resources.word_complete
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedLargeButton
import com.chukchukhaksa.mobile.common.designsystem.component.chip.CchOutlinedChip
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.CchRegularTextField
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.CchSmallTextField
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray6A
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.common.ui.timetableCellColorHexMap
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.toText
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CellEditorRoute(
    viewModel: CellEditorViewModel = koinViewModel(),
    popBackStack: () -> Unit,
    handleException: (Throwable) -> Unit,
    onShowToast: (String, Dp) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is CellEditorSideEffect.HandleException -> handleException(sideEffect.throwable)
            CellEditorSideEffect.PopBackStack -> popBackStack()
            is CellEditorSideEffect.ShowToast -> onShowToast(sideEffect.msg, 130.dp)
            CellEditorSideEffect.ShowAddSuccessCellToast -> onShowToast(getString(Res.string.open_lecture_success_add_cell_toast), 130.dp)
            CellEditorSideEffect.ShowNeedLectureNameToast -> onShowToast(getString(Res.string.add_cell_screen_need_lecture_name), 130.dp)
            CellEditorSideEffect.ShowNeedLocationToast -> onShowToast(getString(Res.string.add_cell_screen_need_location), 130.dp)
            CellEditorSideEffect.ShowNeedProfessorNameToast -> onShowToast(getString(Res.string.add_cell_screen_need_professor_name), 130.dp)
            CellEditorSideEffect.ShowEditSuccessCellToast -> onShowToast(getString(Res.string.open_lecture_success_edit_cell_toast), 130.dp)
        }
    }
    CellEditorScreen(
        uiState = uiState,
        onClickClose = viewModel::popBackStack,
        onValueChangeLectureName = viewModel::updateLectureName,
        onValueChangeProfessorName = viewModel::updateProfessorName,
        onClickDayChip = viewModel::updateCellDay,
        onValueChangeStartPeriod = viewModel::updateCellStartPeriod,
        onValueChangeEndPeriod = viewModel::updateCellEndPeriod,
        onValueChangeLocation = viewModel::updateCellLocation,
        onClickAddButton = viewModel::addCell,
        onClickDeleteButton = viewModel::deleteCell,
        onClickColorChip = viewModel::updateCellColor,
        onClickCompleteButton = viewModel::upsertTimetable,
    )
}

@Composable
fun CellEditorScreen(
    uiState: CellEditorState = CellEditorState(),
    onClickClose: () -> Unit = {},
    onValueChangeLectureName: (String) -> Unit = {},
    onValueChangeProfessorName: (String) -> Unit = {},
    onClickDayChip: (Int, TimetableDay) -> Unit = { _, _ -> },
    onValueChangeStartPeriod: (Int, String) -> Unit = { _, _ -> },
    onValueChangeEndPeriod: (Int, String) -> Unit = { _, _ -> },
    onValueChangeLocation: (Int, String) -> Unit = { _, _ -> },
    onClickAddButton: () -> Unit = {},
    onClickDeleteButton: (Int) -> Unit = {},
    onClickColorChip: (TimetableCellColor) -> Unit = {},
    onClickCompleteButton: () -> Unit = {},
) {
    SuwikiBackground(
        color = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              modifier = Modifier
                .clip(CircleShape)
                .cchClickable(onClick = onClickClose),
              painter = painterResource(resource = Res.drawable.ic_arrow),
              tint = Black100,
              contentDescription = "뒤로가기",
            )
          }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.size(8.dp))

              Column(
                modifier = Modifier.padding(horizontal = 20.dp)
              ) {
                Text(
                  text = stringResource(Res.string.add_cell_screen_lecture_name),
                  style = CchTheme.typography.bodyMdStrong,
                  color = Black100,
                )

                Spacer(modifier = Modifier.size(8.dp))

                CchRegularTextField(
                  value = uiState.lectureName,
                  onValueChanged = onValueChangeLectureName,
                  placeholder = stringResource(Res.string.add_cell_screen_input_lecture_name),
                  isActive = uiState.lectureName.isNotEmpty()
                )
              }

              Spacer(modifier = Modifier.size(24.dp))

              Column(
                modifier = Modifier.padding(horizontal = 20.dp)
              ) {
                Text(
                  text = stringResource(Res.string.add_cell_screen_professor_name),
                  style = CchTheme.typography.bodyMdStrong,
                  color = Black100,
                )

                Spacer(modifier = Modifier.size(8.dp))

                CchRegularTextField(
                  value = uiState.professorName,
                  onValueChanged = onValueChangeProfessorName,
                  placeholder = stringResource(Res.string.add_cell_screen_input_professor_name),
                  isActive = uiState.professorName.isNotEmpty(),
                )
              }

              Spacer(modifier = Modifier.size(24.dp))

              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = stringResource(Res.string.add_cell_screen_time_location),
                  style = CchTheme.typography.bodyMdStrong,
                  color = Black100,
                )

                Text(
                  modifier = Modifier.cchClickable { onClickAddButton() },
                  text = stringResource(Res.string.word_add),
                  style = CchTheme.typography.bodySm,
                  color = Purple600,
                )
              }

              Spacer(modifier = Modifier.size(16.dp))

                uiState.cellStateList.forEachIndexed { index, cell ->
                  Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(
                      modifier = Modifier.cchClickable { onClickDeleteButton(index) },
                      painter = painterResource(Res.drawable.ic_close),
                      contentDescription = "삭제",
                      tint = Gray400,
                    )

                    Spacer(Modifier.width(20.dp))

                    Column {
                      FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        TimetableDay.entries.filter { it != TimetableDay.E_LEARNING }
                          .forEach { day ->
                            CchOutlinedChip(
                              text = day.toText(),
                              isChecked = cell.day == day,
                              onClick = { onClickDayChip(index, day) },
                            )
                          }
                      }

                      Spacer(Modifier.height(12.dp))

                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                        ) {
                          CchSmallTextField(
                            value = cell.startPeriod,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            onValueChange = { onValueChangeStartPeriod(index, it) },
                            showClearButton = false,
                            textStyle = CchTheme.typography.bodySmStrong.copy(textAlign = TextAlign.Center),
                            modifier = Modifier.width(40.dp),
                            placeholder = stringResource(Res.string.add_cell_screen_period),
                          )

                          Spacer(Modifier.width(3.dp))

                          HorizontalDivider(
                            modifier = Modifier.width(7.dp),
                            color = Gray400,
                          )

                          Spacer(Modifier.width(3.dp))

                          CchSmallTextField(
                            value = cell.endPeriod,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            onValueChange = { onValueChangeEndPeriod(index, it) },
                            showClearButton = false,
                            textStyle = CchTheme.typography.bodySmStrong.copy(textAlign = TextAlign.Center),
                            modifier = Modifier.width(40.dp),
                            placeholder = stringResource(Res.string.add_cell_screen_period),
                          )
                        }

                        Spacer(Modifier.width(12.dp))

                        CchSmallTextField(
                          showClearButton = false,
                          value = cell.location,
                          onValueChange = { onValueChangeLocation(index, it) },
                          placeholder = stringResource(Res.string.add_cell_screen_input_location),
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.size(20.dp))

                EditorScreenRow(
                    name = stringResource(Res.string.add_cell_screen_color),
                    verticalAlignment = Alignment.Top,
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                maxItemsInEachRow = 5,
                            ) {
                                TimetableCellColor.entries.forEach {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(Color(timetableCellColorHexMap[it]!!))
                                            .cchClickable(
                                                rippleEnabled = false,
                                                onClick = { onClickColorChip(it) },
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (it == uiState.selectedTimetableCellColor) {
                                            Icon(
                                                modifier = Modifier.size(16.dp),
                                                painter = painterResource(resource = Res.drawable.ic_align_checked),
                                                contentDescription = null,
                                                tint = White,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                )
            }

          Spacer(modifier = Modifier.size(20.dp))

            CchBasicButton(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = stringResource(resource = Res.string.word_complete),
              enable = true,
                onClick = onClickCompleteButton,
            )
            Spacer(modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun EditorScreenRow(
    modifier: Modifier = Modifier,
    name: String,
    verticalAlignment: Alignment.Vertical,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalAlignment = verticalAlignment,
    ) {
        Text(
            modifier = Modifier,
            text = name,
            style = CchTheme.typography.bodyMdStrong,
            color = Black100,
        )

      Spacer(Modifier.width(50.dp))

        content()
    }
}

@Preview
@Composable
fun CellEditorScreenPreview() {
  CchTheme {
      CellEditorScreen()
  }
}
