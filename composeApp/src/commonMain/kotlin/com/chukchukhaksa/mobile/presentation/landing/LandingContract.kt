package com.chukchukhaksa.mobile.presentation.landing

data class LandingState(
    val isLoading: Boolean = false,
)

sealed interface LandingSideEffect {
    data object NavigateHome : LandingSideEffect
    data class ShowToast(val message: String) : LandingSideEffect
    data class HandleException(val throwable: Throwable) : LandingSideEffect
}
