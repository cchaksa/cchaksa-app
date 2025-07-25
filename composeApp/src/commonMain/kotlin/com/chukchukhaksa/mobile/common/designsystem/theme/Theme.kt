package com.chukchukhaksa.mobile.common.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun SuwikiTheme(
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalTypography provides Typography(),
  ) {
    MaterialTheme(
      colorScheme = lightColorScheme(
        background = GrayFB,
      ),
      content = content,
    )
  }
}

@Composable
fun CCHaksaTheme(
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalCCHaksaTypography provides CCHaksaTypography(),
  ) {
    MaterialTheme(
      colorScheme = lightColorScheme(
        background = White100,
      ),
      content = content,
    )
  }
}

object SuwikiTheme {
  val typography: SuwikiTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalTypography.current
}

object CCHaksaTheme {
  val typography: CCHaksaTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalCCHaksaTypography.current
}
