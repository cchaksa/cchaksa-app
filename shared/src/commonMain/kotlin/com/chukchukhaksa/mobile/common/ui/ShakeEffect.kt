package com.chukchukhaksa.mobile.common.ui

import androidx.compose.runtime.Composable

/** 화면이 보이는 동안 기기를 흔들면 [onShake]를 부른다. 디버그 화면 진입에 쓴다. */
@Composable
expect fun ShakeEffect(onShake: () -> Unit)
