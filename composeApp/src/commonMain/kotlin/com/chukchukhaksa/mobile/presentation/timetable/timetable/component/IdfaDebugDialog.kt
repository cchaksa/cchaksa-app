package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray100
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray300
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray500
import com.chukchukhaksa.mobile.common.designsystem.theme.Red400
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.kmp.AdvertisingIdInfo
import com.chukchukhaksa.mobile.common.ui.cchClickable

/**
 * 디버그 전용 IDFA 진단 다이얼로그.
 *
 * 시간표 탭 3연타 제스처로 노출된다. IDFA를 선택·복사 가능한 형태로 보여주고,
 * 조회 실패 시(ATT 미허용 등) 원인 추적용 진단 문자열을 함께 노출한다.
 * 디자인 시스템 컴포넌트가 아니라 디버그 도구이므로 색상·타이포 토큰만 재사용한다.
 */
@Composable
fun IdfaDebugDialog(
    info: AdvertisingIdInfo,
    onClickCopy: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(White100)
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Text(
                text = "디버그 · IDFA",
                style = CchTheme.typography.bodyLgStrong,
                color = Black100,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // IDFA 값: 길게 눌러 부분 선택·복사도 가능하도록 SelectionContainer로 감싼다.
            SelectionContainer {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray100)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    text = info.id ?: "(IDFA 미조회)",
                    style = CchTheme.typography.bodySm,
                    fontFamily = FontFamily.Monospace,
                    color = if (info.isValid) Black100 else Red400,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = info.diagnostics,
                style = CchTheme.typography.bodyXs,
                color = if (info.isValid) Gray500 else Red400,
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                DialogButton(
                    modifier = Modifier.weight(1f),
                    text = "닫기",
                    textColor = Gray500,
                    background = White100,
                    borderColor = Gray300,
                    onClick = onDismiss,
                )
                Spacer(modifier = Modifier.width(8.dp))
                DialogButton(
                    modifier = Modifier.weight(1f),
                    text = "복사",
                    textColor = White100,
                    background = Black100,
                    borderColor = null,
                    onClick = onClickCopy,
                )
            }
        }
    }
}

@Composable
private fun DialogButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    background: Color,
    borderColor: Color?,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .then(
                if (borderColor != null) {
                    Modifier.border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .cchClickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = CchTheme.typography.bodyMdStrong,
            color = textColor,
        )
    }
}
