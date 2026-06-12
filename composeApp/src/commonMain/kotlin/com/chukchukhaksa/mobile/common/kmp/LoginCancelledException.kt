package com.chukchukhaksa.mobile.common.kmp

/**
 * 사용자가 소셜 로그인(카카오/애플)을 직접 취소했을 때 던지는 예외.
 *
 * 취소는 실패가 아니므로 LandingViewModel에서 별도로 걸러내, "네트워크 오류" 토스트나
 * Crashlytics 기록 없이 조용히 무시한다. (코루틴 취소가 아니라 일반 예외이므로
 * runCatchingIgnoreCancelled는 이를 Result.failure로 전달한다.)
 */
class LoginCancelledException : Exception("Login cancelled by user")
