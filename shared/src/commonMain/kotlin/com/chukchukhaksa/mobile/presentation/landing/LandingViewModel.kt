package com.chukchukhaksa.mobile.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.analytics.AnalyticsUserSync
import com.chukchukhaksa.mobile.common.extension.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.common.kmp.AppleSignInClient
import com.chukchukhaksa.mobile.common.kmp.KakaoSignInClient
import com.chukchukhaksa.mobile.common.kmp.LoginCancelledException
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.data.auth.model.SignInResult
import com.chukchukhaksa.mobile.data.auth.repository.AuthRepository
import com.chukchukhaksa.mobile.presentation.webview.WebViewPreloader
import kotlinx.coroutines.launch

class LandingViewModel(
    private val kakaoSignInClient: KakaoSignInClient,
    private val appleSignInClient: AppleSignInClient,
    private val authRepository: AuthRepository,
    private val webViewPreloader: WebViewPreloader,
    private val analyticsUserSync: AnalyticsUserSync,
) : ViewModel() {

    val mviStore: MviStore<LandingState, LandingSideEffect> = mviStore(LandingState())

    fun onKakaoLogin(context: Any? = null) = login(LoginProvider.KAKAO) {
        val kakaoResult = kakaoSignInClient.signIn(context)
        authRepository.signIn(provider = "KAKAO", idToken = kakaoResult.idToken, nonce = kakaoResult.nonce)
    }

    fun onAppleLogin() = login(LoginProvider.APPLE) {
        val appleResult = appleSignInClient.signIn()
        authRepository.signIn(provider = "APPLE", idToken = appleResult.identityToken, nonce = appleResult.nonce)
    }

    private fun login(provider: LoginProvider, signIn: suspend () -> SignInResult) {
        if (mviStore.uiState.value.isLoading) return
        mviStore.setState { copy(loadingProvider = provider) }

        viewModelScope.launch {
            try {
                runCatchingIgnoreCancelled { signIn() }
                    .onSuccess { signInResult ->
                        // 세션 쿠키 교환·WebView preload(setCookie 포함)가 끝난 뒤에 화면을 전환한다.
                        // 그 전까지는 로딩 표시를 유지해 빈 쿠키로 WebView가 먼저 로드되는 race를 막는다.
                        webViewPreloader.preloadAndAwait()
                        // Amplitude 사용자 식별 + user properties. 실패해도 로그인/네비게이션을 막지 않는다.
                        analyticsUserSync.identify()
                        analyticsUserSync.syncUserProperties()
                        mviStore.postSideEffect(LandingSideEffect.NavigateHome(signInResult.isPortalLinked))
                    }
                    .onFailure { throwable ->
                        // 사용자가 직접 취소한 경우는 실패가 아니므로 토스트·기록 없이 무시한다.
                        if (throwable !is LoginCancelledException) {
                            mviStore.postSideEffect(LandingSideEffect.HandleException(throwable))
                        }
                    }
            } finally {
                mviStore.setState { copy(loadingProvider = null) }
            }
        }
    }
}
