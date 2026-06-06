package com.chukchukhaksa.mobile.presentation.landing

data class LandingState(
    val loadingProvider: LoginProvider? = null,
) {
    val isLoading: Boolean get() = loadingProvider != null
}

enum class LoginProvider { KAKAO, APPLE }

sealed interface LandingSideEffect {
    data class NavigateHome(val isPortalLinked: Boolean) : LandingSideEffect
    data class ShowToast(val message: String) : LandingSideEffect
    data class HandleException(val throwable: Throwable) : LandingSideEffect
}
