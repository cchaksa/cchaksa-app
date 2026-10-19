package com.chukchukhaksa.mobile.presentation.debug.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.chukchukhaksa.mobile.presentation.debug.DebugRoute
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkMode
import com.chukchukhaksa.mobile.common.ui.cchComposable

object DebugRoute {
  const val route = "debug"
}

fun NavController.navigateDebug() {
  navigate(DebugRoute.route)
}

/** 디버그 빌드에서만 등록한다. */
fun NavGraphBuilder.debugNavGraph(
  popBackStack: () -> Unit,
  navigatePortalLink: (PortalLinkMode) -> Unit,
) {
  cchComposable(route = DebugRoute.route) {
    DebugRoute(
      popBackStack = popBackStack,
      navigatePortalLink = navigatePortalLink,
    )
  }
}
