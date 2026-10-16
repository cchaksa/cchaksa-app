package com.chukchukhaksa.mobile.presentation.webview.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chukchukhaksa.mobile.common.ui.decodeFromUri
import com.chukchukhaksa.mobile.common.ui.encodeToUri
import com.chukchukhaksa.mobile.presentation.webview.WebViewRoute
import kotlinx.serialization.json.Json

fun NavController.navigateWebView(url: String) {
  navigate(WebViewRouteSpec.buildRoute(url))
}

fun NavGraphBuilder.webViewNavGraph(
  popBackStack: () -> Unit,
  onNavigateWebView: (String) -> Unit,
  navigateToLanding: () -> Unit,
) {
  composable(
    route = WebViewRouteSpec.pattern(),
    arguments = listOf(
      navArgument(WebViewRouteSpec.URL_ARG) {
        type = NavType.StringType
      },
    ),
  ) { backStackEntry ->
    val encoded = backStackEntry.savedStateHandle.get<String>(WebViewRouteSpec.URL_ARG).orEmpty()
    val url = Json.decodeFromUri<String>(encoded)
    WebViewRoute(
      url = url,
      popBackStack = popBackStack,
      onNavigateWebView = onNavigateWebView,
      navigateToLanding = navigateToLanding,
    )
  }
}

object WebViewRouteSpec {
  const val route = "webview"
  const val URL_ARG = "url"

  fun buildRoute(url: String): String = "$route/${Json.encodeToUri(url)}"
  fun pattern(): String = "$route/{$URL_ARG}"
}
