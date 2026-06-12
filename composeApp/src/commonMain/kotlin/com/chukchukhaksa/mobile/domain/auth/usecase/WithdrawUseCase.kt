package com.chukchukhaksa.mobile.domain.auth.usecase

import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewHolder
import com.chukchukhaksa.mobile.domain.analytics.usecase.ClearAnalyticsUserIdUseCase
import com.chukchukhaksa.mobile.domain.auth.repository.AuthRepository
import com.chukchukhaksa.mobile.domain.webview.ExchangeWebSessionUseCase

/**
 * 회원 탈퇴 시 앱의 로컬 인증 상태를 정리한다.
 *
 * 실제 탈퇴 API 호출은 웹에서 수행하며, 이 use case는 앱이 로그아웃 상태를 반영하도록
 * 로컬 토큰과 웹 세션 쿠키를 지우고, 다음 로그인 시 최신 페이지가 로드되도록 홈 웹뷰 홀더를 리셋한다.
 */
class WithdrawUseCase(
  private val authRepository: AuthRepository,
  private val exchangeWebSession: ExchangeWebSessionUseCase,
  private val webViewHolder: WebViewHolder,
  private val clearAnalyticsUserId: ClearAnalyticsUserIdUseCase,
) {
  suspend operator fun invoke() {
    authRepository.clearTokens()
    exchangeWebSession.clear()
    webViewHolder.reset()
    clearAnalyticsUserId()
  }
}
