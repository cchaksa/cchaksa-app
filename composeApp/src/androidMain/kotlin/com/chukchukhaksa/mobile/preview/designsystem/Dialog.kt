package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.component.dialog.CchDialog
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme

@Preview
@Composable
fun CchDialogTwoButtonPreview() {
    CchTheme {
        CchDialog(
            headerText = "알림",
            bodyText = "학교 연동없이 이용시\n'시간표 만들기'만 이용 가능합니다.",
            confirmButtonText = "확인",
            dismissButtonText = "취소",
            onDismissRequest = {},
            onClickConfirm = {},
            onClickDismiss = {},
        )
    }
}

@Preview
@Composable
fun CchDialogSingleButtonPreview() {
    CchTheme {
        CchDialog(
            headerText = "최신 버전 업데이트",
            bodyText = "최신버전 앱으로 업데이트를 위해\n스토어로 이동합니다.",
            confirmButtonText = "확인",
            onDismissRequest = {},
            onClickConfirm = {},
        )
    }
}
