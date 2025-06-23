package com.chukchukhaksa.mobile.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.shadow.bottomNavigationShadow
import com.chukchukhaksa.mobile.common.ui.shadow.cardShadow

@Preview(showBackground = true)
@Composable
fun BottomNavigationShadowPreview() {
    Box(
        modifier = Modifier.padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .bottomNavigationShadow(borderRadius = 5.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(White),
        ) {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardShadowPreview() {
    Box(
        modifier = Modifier.padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .cardShadow(borderRadius = 5.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(White),
        ) {
        }
    }
}