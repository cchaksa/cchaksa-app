package com.chukchukhaksa.mobile.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.auth.usecase.AppleLoginUseCase
import com.chukchukhaksa.mobile.domain.auth.usecase.KakaoLoginUseCase
import com.chukchukhaksa.mobile.domain.webview.WebViewPreloader
import kotlinx.coroutines.launch

class LandingViewModel(
    private val kakaoLoginUseCase: KakaoLoginUseCase,
    private val appleLoginUseCase: AppleLoginUseCase,
    private val webViewPreloader: WebViewPreloader,
) : ViewModel() {

    val mviStore: MviStore<LandingState, LandingSideEffect> = mviStore(LandingState())

    fun onKakaoLogin(context: Any? = null) {
        if (mviStore.uiState.value.isLoading) return
        mviStore.setState { copy(loadingProvider = LoginProvider.KAKAO) }

        viewModelScope.launch {
            try {
                kakaoLoginUseCase(context)
                    .onSuccess { signInResult ->
                        // 세션 쿠키 교환·WebView preload(setCookie 포함)가 끝난 뒤에 화면을 전환한다.
                        // 그 전까지는 로딩 표시를 유지해 빈 쿠키로 WebView가 먼저 로드되는 race를 막는다.
                        webViewPreloader.preloadAndAwait()
                        mviStore.postSideEffect(LandingSideEffect.NavigateHome(signInResult.isPortalLinked))
                    }
                    .onFailure { throwable ->
                        mviStore.postSideEffect(LandingSideEffect.HandleException(throwable))
                    }
            } finally {
                mviStore.setState { copy(loadingProvider = null) }
            }
        }
    }

    fun onAppleLogin() {
        if (mviStore.uiState.value.isLoading) return
        mviStore.setState { copy(loadingProvider = LoginProvider.APPLE) }

        viewModelScope.launch {
            try {
                appleLoginUseCase()
                    .onSuccess { signInResult ->
                        // 세션 쿠키 교환·WebView preload(setCookie 포함)가 끝난 뒤에 화면을 전환한다.
                        webViewPreloader.preloadAndAwait()
                        mviStore.postSideEffect(LandingSideEffect.NavigateHome(signInResult.isPortalLinked))
                    }
                    .onFailure { throwable ->
                        mviStore.postSideEffect(LandingSideEffect.HandleException(throwable))
                    }
            } finally {
                mviStore.setState { copy(loadingProvider = null) }
            }
        }
    }
}
