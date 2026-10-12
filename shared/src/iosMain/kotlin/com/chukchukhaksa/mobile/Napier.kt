package com.chukchukhaksa.mobile

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun initializeNapier() {
    if (Platform.isDebugBinary) {
        Napier.base(DebugAntilog())
    }
}