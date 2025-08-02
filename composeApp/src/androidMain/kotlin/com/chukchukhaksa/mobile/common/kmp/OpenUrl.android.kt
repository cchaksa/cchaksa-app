package com.chukchukhaksa.mobile.common.kmp

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

actual fun openUrl(context: Any?, url: String) {
    val ctx = context as? Context ?: return
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    ctx.startActivity(intent)
}
