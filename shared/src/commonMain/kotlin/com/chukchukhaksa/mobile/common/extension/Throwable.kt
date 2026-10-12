package com.chukchukhaksa.mobile.common.extension

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import io.github.aakira.napier.Napier

fun Throwable.record() {
    recordException(RuntimeException(this))
}

private fun recordException(
    e: Throwable,
) {
    Napier.e("recordException $e", e)
    Firebase.crashlytics.recordException(e)
}