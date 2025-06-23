package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.edit_timetable_cell_bottom_sheet_info
import chukchukhaksa.composeapp.generated.resources.word_do_delete
import chukchukhaksa.composeapp.generated.resources.word_do_edit
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.SuwikiBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedLargeButton
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.toText
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimetableCellBottomSheet(
    onDismissRequest: () -> Unit = {},
    cell: TimetableCell = TimetableCell(color = TimetableCellColor.GRAY_DARK),
    isSheetOpen: Boolean = false,
    onClickDeleteButton: (TimetableCell) -> Unit = {},
    onClickEditButton: (TimetableCell) -> Unit = {},
) {
    SuwikiBottomSheet(
        isSheetOpen = isSheetOpen,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = cell.name, style = SuwikiTheme.typography.header2)
                Text(
                    modifier = Modifier.align(Alignment.Bottom),
                    text = cell.professor,
                    style = SuwikiTheme.typography.header6
                )
            }

            val infoText = if (cell.day == TimetableDay.E_LEARNING) {
                cell.day.toText()
            } else {
                stringResource(
                    Res.string.edit_timetable_cell_bottom_sheet_info,
                    cell.location,
                    cell.day.toText(),
                    cell.startPeriod,
                    cell.endPeriod,
                )
            }

            Text(
                text = infoText,
                style = SuwikiTheme.typography.body3,
            )

            Spacer(modifier = Modifier.size(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SuwikiContainedLargeButton(
                    modifier = Modifier.weight(1f),
                    enabled = false,
                    text = stringResource(Res.string.word_do_delete),
                    onClick = { onClickDeleteButton(cell) },
                )

                SuwikiContainedLargeButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.word_do_edit),
                    onClick = { onClickEditButton(cell) },
                )
            }
        }
    }
}
