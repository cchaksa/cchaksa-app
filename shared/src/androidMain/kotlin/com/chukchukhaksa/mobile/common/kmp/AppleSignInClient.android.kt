package com.chukchukhaksa.mobile.common.kmp

actual class AppleSignInClient actual constructor() {
    actual suspend fun signIn(): AppleSignInResult {
        throw UnsupportedOperationException("Apple Sign In is not supported on Android")
    }
}
