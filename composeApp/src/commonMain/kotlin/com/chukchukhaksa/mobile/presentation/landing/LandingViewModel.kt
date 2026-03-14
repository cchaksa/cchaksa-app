package com.chukchukhaksa.mobile.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.auth.usecase.KakaoLoginUseCase
import kotlinx.coroutines.launch

class LandingViewModel(
    private val kakaoLoginUseCase: KakaoLoginUseCase,
) : ViewModel() {

    val mviStore: MviStore<LandingState, LandingSideEffect> = mviStore(LandingState())

    fun onKakaoLogin(context: Any? = null) {
        if (mviStore.uiState.value.isLoading) return
        mviStore.setState { copy(isLoading = true) }

        viewModelScope.launch {
            try {
                kakaoLoginUseCase(context)
                    .onSuccess {
                        mviStore.postSideEffect(LandingSideEffect.NavigateHome)
                    }
                    .onFailure { throwable ->
                        mviStore.postSideEffect(LandingSideEffect.HandleException(throwable))
                    }
            } finally {
                mviStore.setState { copy(isLoading = false) }
            }
        }
    }

    fun onAppleLogin() {
        mviStore.postSideEffect(LandingSideEffect.ShowToast("준비 중입니다"))
    }
}
