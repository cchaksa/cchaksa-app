package com.chukchukhaksa.mobile.presentation.portallink.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chukchukhaksa.mobile.presentation.portallink.PortalLinkRoute

/** 포털 연동 화면의 용도. 처음 연동하는 화면과 학업 정보를 업데이트하는 화면은 폼이 같고 안내와 이동만 다르다. */
enum class PortalLinkMode(
  /** 이 화면이 대체한 웹 경로. */
  val webPath: String,
) {
  LINK(webPath = "/mpa/portal-login"),
  RESYNC(webPath = "/mpa/resync/login"),
  ;

  companion object {
    /**
     * 웹뷰로 열려던 URL이 네이티브로 옮긴 포털 연동 화면이면 그 용도를 돌려준다.
     * 웹 홈이 보내는 이동 요청과 광고 게이트를 거친 이동이 모두 이 판정을 거쳐 네이티브 화면으로 바뀐다.
     */
    fun fromWebUrl(url: String): PortalLinkMode? {
      val afterScheme = url.substringAfter("://", missingDelimiterValue = "")
      val host = afterScheme.substringBefore('/')
      if (host != CCHAKSA_DOMAIN && !host.endsWith(".$CCHAKSA_DOMAIN")) return null
      val path = afterScheme.removePrefix(host)
        .substringBefore('?')
        .substringBefore('#')
        .trimEnd('/')
      return entries.firstOrNull { it.webPath == path }
    }

    private const val CCHAKSA_DOMAIN = "cchaksa.com"
  }
}

object PortalLinkRouteSpec {
  const val route = "portal-link"
  const val MODE_ARG = "mode"

  fun buildRoute(mode: PortalLinkMode): String = "$route/${mode.name}"
  fun pattern(): String = "$route/{$MODE_ARG}"
}

fun NavController.navigatePortalLink(mode: PortalLinkMode) {
  navigate(PortalLinkRouteSpec.buildRoute(mode))
}

fun NavGraphBuilder.portalLinkNavGraph(
  popBackStack: () -> Unit,
) {
  composable(
    route = PortalLinkRouteSpec.pattern(),
    arguments = listOf(
      navArgument(PortalLinkRouteSpec.MODE_ARG) {
        type = NavType.StringType
      },
    ),
  ) {
    PortalLinkRoute(popBackStack = popBackStack)
  }
}
