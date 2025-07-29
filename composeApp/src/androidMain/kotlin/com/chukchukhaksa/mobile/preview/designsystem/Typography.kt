package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.CCHaksaTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme

@Preview(showBackground = true)
@Composable
fun CCHaksaTypographyPreview() {
    CCHaksaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Title ExtraLarge2 (32px)",
                style = CchTheme.typography.titleExlg2,
            )

            Text(
                text = "Title ExtraLarge (28px)",
                style = CchTheme.typography.titleExlg,
            )

            Text(
                text = "Title Large (24px)",
                style = CchTheme.typography.titleLg,
            )

            Text(
                text = "Body ExtraLarge (24px)",
                style = CchTheme.typography.bodyExlg,
            )

            Text(
                text = "Body Large Strong (18px)",
                style = CchTheme.typography.bodyLgStrong,
            )

            Text(
                text = "Body Large (18px)",
                style = CchTheme.typography.bodyLg,
            )

            Text(
                text = "Body Medium Strong (16px)",
                style = CchTheme.typography.bodyMdStrong,
            )

            Text(
                text = "Body Medium (16px)",
                style = CchTheme.typography.bodyMd,
            )

            Text(
                text = "Body Small (14px)",
                style = CchTheme.typography.bodySm,
            )

            Text(
                text = "Body Extra Small (12px)",
                style = CchTheme.typography.bodyXs,
            )
        }
    }
}
