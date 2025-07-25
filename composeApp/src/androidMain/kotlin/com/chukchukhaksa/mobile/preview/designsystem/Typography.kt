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
                style = CCHaksaTheme.typography.titleExlg2,
            )
            
            Text(
                text = "Title ExtraLarge (28px)",
                style = CCHaksaTheme.typography.titleExlg,
            )
            
            Text(
                text = "Title Large (24px)",
                style = CCHaksaTheme.typography.titleLg,
            )
            
            Text(
                text = "Body ExtraLarge (24px)",
                style = CCHaksaTheme.typography.bodyExlg,
            )
            
            Text(
                text = "Body Large Strong (18px)",
                style = CCHaksaTheme.typography.bodyLgStrong,
            )
            
            Text(
                text = "Body Large (18px)",
                style = CCHaksaTheme.typography.bodyLg,
            )
            
            Text(
                text = "Body Medium Strong (16px)",
                style = CCHaksaTheme.typography.bodyMdStrong,
            )
            
            Text(
                text = "Body Medium (16px)",
                style = CCHaksaTheme.typography.bodyMd,
            )
            
            Text(
                text = "Body Small (14px)",
                style = CCHaksaTheme.typography.bodySm,
            )
            
            Text(
                text = "Body Extra Small (12px)",
                style = CCHaksaTheme.typography.bodyXs,
            )
        }
    }
}