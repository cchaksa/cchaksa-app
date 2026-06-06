package com.chukchukhaksa.mobile.common.designsystem.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray300
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray500
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable

@Composable
fun CchDialog(
    modifier: Modifier = Modifier,
    headerText: String,
    bodyText: String,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    onDismissRequest: () -> Unit,
    onClickConfirm: () -> Unit,
    onClickDismiss: () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(White100)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = headerText,
                style = CchTheme.typography.bodyLgStrong,
                color = Black100,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = bodyText,
                style = CchTheme.typography.bodyMd,
                color = Black100,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (dismissButtonText != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DialogOutlinedButton(
                        modifier = Modifier.weight(1f),
                        text = dismissButtonText,
                        onClick = onClickDismiss,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DialogFilledButton(
                        modifier = Modifier.weight(1f),
                        text = confirmButtonText,
                        onClick = onClickConfirm,
                    )
                }
            } else {
                DialogFilledButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = confirmButtonText,
                    onClick = onClickConfirm,
                )
            }
        }
    }
}

@Composable
private fun DialogFilledButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Black100)
            .cchClickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = CchTheme.typography.bodyMdStrong,
            color = White100,
        )
    }
}

@Composable
private fun DialogOutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(White100)
            .border(width = 1.dp, color = Gray300, shape = RoundedCornerShape(12.dp))
            .cchClickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = CchTheme.typography.bodyMdStrong,
            color = Gray500,
        )
    }
}
