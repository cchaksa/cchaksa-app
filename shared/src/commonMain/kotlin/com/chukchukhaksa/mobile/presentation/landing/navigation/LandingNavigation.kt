package com.chukchukhaksa.mobile.presentation.landing.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.chukchukhaksa.mobile.presentation.landing.LandingRoute

object LandingRoute {
    const val route = "landing"
}

fun NavGraphBuilder.landingNavGraph(
    navigateToHome: (isPortalLinked: Boolean) -> Unit,
) {
    composable(route = LandingRoute.route) {
        LandingRoute(
            navigateToHome = navigateToHome,
        )
    }
}
