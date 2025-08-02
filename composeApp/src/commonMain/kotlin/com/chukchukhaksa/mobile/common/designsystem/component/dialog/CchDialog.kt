package com.chukchukhaksa.mobile.common.designsystem.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.chukchukhaksa.mobile.common.designsystem.theme.Black
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.Primary
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
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
    Dialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(White100)
                    .padding(top = 20.dp, bottom = 20.dp, start = 24.dp, end = 24.dp),
            ) {
                Text(
                    text = headerText,
                    style = CchTheme.typography.bodyMdStrong,
                    color = Black,
                )
                Text(
                    text = bodyText,
                    style = CchTheme.typography.bodyMd,
                    color = Black,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (dismissButtonText != null) {
                        Text(
                            modifier = Modifier.cchClickable(
                                rippleEnabled = false,
                                onClick = onClickDismiss
                            ),
                            text = dismissButtonText,
                            style = CchTheme.typography.bodyLgStrong,
                            color = Gray400,
                        )
                      Spacer(modifier = Modifier.width(58.dp))
                    }

                    Text(
                        modifier = Modifier.cchClickable(
                            rippleEnabled = false,
                            onClick = onClickConfirm
                        ),
                        text = confirmButtonText,
                        style = CchTheme.typography.bodyLgStrong,
                        color = Purple600,
                    )
                }
            }
        },
    )
}
