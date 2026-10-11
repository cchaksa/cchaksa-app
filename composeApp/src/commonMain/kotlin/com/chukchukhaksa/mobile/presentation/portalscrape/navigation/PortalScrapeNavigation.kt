package com.chukchukhaksa.mobile.presentation.portalscrape.navigation

import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.chukchukhaksa.mobile.presentation.portalscrape.PortalScrapeRoute

/** 디버그 빌드 전용 포털 스크래핑 테스트 화면. */
object PortalScrapeRoute {
  const val route = "portal-scrape-test"
}

fun NavController.navigatePortalScrapeTest() {
  navigate(PortalScrapeRoute.route)
}

fun NavGraphBuilder.portalScrapeNavGraph(
  popBackStack: () -> Unit,
  onShowToast: (String, Dp) -> Unit,
) {
  composable(route = PortalScrapeRoute.route) {
    PortalScrapeRoute(
      popBackStack = popBackStack,
      onShowToast = onShowToast,
    )
  }
}
