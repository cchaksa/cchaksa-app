package com.chukchukhaksa.mobile.common.kmp

import java.security.MessageDigest

actual fun sha256Hex(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
