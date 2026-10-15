package com.chukchukhaksa.mobile.presentation.webview

import com.chukchukhaksa.mobile.common.analytics.AnalyticsUserSync
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewHolder
import com.chukchukhaksa.mobile.data.auth.repository.AuthRepository

/**
 * 웹이 보낸 [BridgeAction.Withdraw]를 받아 앱의 로컬 인증 상태를 정리한다.
 *
 * 실제 탈퇴 API 호출은 웹에서 수행하며, 이 클래스는 앱이 로그아웃 상태를 반영하도록
 * 로컬 토큰과 웹 세션 쿠키를 지우고, 다음 로그인 시 최신 페이지가 로드되도록 홈 웹뷰 홀더를 리셋한다.
 */
class WithdrawHandler(
  private val authRepository: AuthRepository,
  private val webSessionManager: WebSessionManager,
  private val webViewHolder: WebViewHolder,
  private val analyticsUserSync: AnalyticsUserSync,
) {
  suspend operator fun invoke() {
    authRepository.clearTokens()
    webSessionManager.clear()
    webViewHolder.reset()
    analyticsUserSync.clear()
  }
}
