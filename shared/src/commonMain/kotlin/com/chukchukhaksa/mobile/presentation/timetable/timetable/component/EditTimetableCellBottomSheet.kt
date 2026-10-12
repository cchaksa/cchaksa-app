package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.edit_timetable_cell_bottom_sheet_info
import chukchukhaksa.composeapp.generated.resources.word_do_delete
import chukchukhaksa.composeapp.generated.resources.word_do_edit
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.CchBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray300
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable
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
    onClickDeleteButton: (TimetableCell) -> Unit = {},
    onClickEditButton: (TimetableCell) -> Unit = {},
) {
    CchBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = cell.name, style = CchTheme.typography.bodyLgStrong)
                Text(
                    modifier = Modifier.align(Alignment.Bottom),
                    text = cell.professor,
                    style = CchTheme.typography.bodyMd
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
                style = CchTheme.typography.bodyMdStrong,
            )

            Spacer(modifier = Modifier.size(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .cchClickable { onClickDeleteButton(cell) }
                        .background(White100)
                        .border(
                            width = 1.dp,
                            color = Gray300,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(18.dp),
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        text = stringResource(Res.string.word_do_delete),
                        color = Gray400,
                        style = CchTheme.typography.bodyLgStrong,
                        textAlign = TextAlign.Center,
                    )
                }

                CchBasicButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.word_do_edit),
                    enable = true,
                    onClick = { onClickEditButton(cell) },
                )
            }
        }
    }
}
