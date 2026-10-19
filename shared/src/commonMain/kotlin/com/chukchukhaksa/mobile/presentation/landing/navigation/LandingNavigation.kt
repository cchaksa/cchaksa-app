package com.chukchukhaksa.mobile.presentation.landing.navigation

import androidx.compose.ui.unit.Dp
import androidx.navigation.NavGraphBuilder
import com.chukchukhaksa.mobile.presentation.landing.LandingRoute
import com.chukchukhaksa.mobile.common.ui.cchComposable

object LandingRoute {
    const val route = "landing"
}

fun NavGraphBuilder.landingNavGraph(
    handleException: (Throwable) -> Unit,
    onShowToast: (String, Dp) -> Unit,
    navigateToHome: (isPortalLinked: Boolean) -> Unit,
) {
    cchComposable(route = LandingRoute.route) {
        LandingRoute(
            handleException = handleException,
            onShowToast = onShowToast,
            navigateToHome = navigateToHome,
        )
    }
}
