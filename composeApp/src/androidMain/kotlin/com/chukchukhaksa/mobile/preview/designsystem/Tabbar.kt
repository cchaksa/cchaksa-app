package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.designsystem.component.tabbar.SuwikiTabBar
import com.chukchukhaksa.mobile.common.designsystem.component.tabbar.TabTitle
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme

@Preview
@Composable
fun SuwikiTabBarPreview() {
    SuwikiTheme {
        var selectedTabPosition by remember { mutableIntStateOf(0) }

        val items = listOf(
            "메뉴(0)",
            "메뉴(0)",
        )

        SuwikiTabBar(
            selectedTabPosition = selectedTabPosition,
        ) {
            items.forEachIndexed { index, s ->
                TabTitle(title = s, position = index, selected = index == selectedTabPosition) { selectedTabPosition = index }
            }
        }
    }
}