package com.chukchukhaksa.mobile.presentation.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import com.chukchukhaksa.mobile.common.designsystem.component.webview.CchWebView
import com.chukchukhaksa.mobile.common.designsystem.component.webview.CchWebViewController
import com.chukchukhaksa.mobile.common.designsystem.component.webview.DebugWebViewBadge
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.common.designsystem.component.webview.rememberCchWebViewController
import com.chukchukhaksa.mobile.common.designsystem.component.loading.WebViewLoadingShimmer
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.PlatformBackHandler
import com.chukchukhaksa.mobile.domain.auth.usecase.WithdrawUseCase
import com.chukchukhaksa.mobile.domain.webview.ExchangeWebSessionUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.TimeSource

private const val DUPLICATE_PUSH_DEBOUNCE_MS = 500L

@Composable
fun WebViewRoute(
  url: String,
  popBackStack: () -> Unit,
  onNavigateWebView: (String) -> Unit,
  navigateToLanding: () -> Unit,
) {
  val controller = rememberCchWebViewController()
  val exchangeWebSession: ExchangeWebSessionUseCase = koinInject()
  val homeRedirectEventBus: HomeRedirectEventBus = koinInject()
  val withdraw: WithdrawUseCase = koinInject()
  val scope = rememberCoroutineScope()
  val cookies by exchangeWebSession.cookies.collectAsStateWithLifecycle()

  WebViewRouteContent(
    url = url,
    cookies = cookies,
    popBackStack = popBackStack,
    controller = controller,
    onNavigateWebView = onNavigateWebView,
    onRedirectToHome = homeRedirectEventBus::redirectToHome,
    // 회원 탈퇴 → 로컬 토큰·세션 정리 후 로그인(랜딩) 화면으로 이동.
    onWithdraw = {
      scope.launch {
        withdraw()
        navigateToLanding()
      }
    },
  )
}

@Composable
private fun WebViewRouteContent(
  url: String,
  cookies: List<WebViewCookie>,
  popBackStack: () -> Unit,
  controller: CchWebViewController,
  onNavigateWebView: (String) -> Unit,
  onRedirectToHome: (reloadWebView: Boolean) -> Unit,
  onWithdraw: () -> Unit,
) {
  PlatformBackHandler(enabled = controller.canGoBack) {
    controller.goBack()
  }
  val currentHost = remember(url) {
    url.substringAfter("://").substringBefore("/")
  }
  var lastPushedUrl by rememberSaveable { mutableStateOf<String?>(null) }
  val lastPushMarkHolder = remember { object { var mark: TimeSource.Monotonic.ValueTimeMark? = null } }

  Box(modifier = Modifier.fillMaxSize()) {
   Column(
    modifier = Modifier
      .fillMaxSize()
      .background(White100)
      .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.ime)),
   ) {
    Box(modifier = Modifier.fillMaxSize()) {
    CchWebView(
      url = url,
      controller = controller,
      cookies = cookies,
      onBridgeMessage = { message: BridgeMessage ->
        Napier.w(tag = "BridgeAction") { "raw message: $message" }
        when (val action = message.toAction(currentHost)) {
          is BridgeAction.NavigateWebView -> {
            val mark = lastPushMarkHolder.mark
            val elapsedMs = mark?.elapsedNow()?.inWholeMilliseconds ?: Long.MAX_VALUE
            if (lastPushedUrl == action.absoluteUrl && elapsedMs < DUPLICATE_PUSH_DEBOUNCE_MS) {
              Napier.w(tag = "BridgeAction") {
                "Skipped duplicate push within ${elapsedMs}ms: ${action.absoluteUrl}"
              }
            } else {
              lastPushedUrl = action.absoluteUrl
              lastPushMarkHolder.mark = TimeSource.Monotonic.markNow()
              onNavigateWebView(action.absoluteUrl)
            }
          }

          is BridgeAction.RedirectToHome -> onRedirectToHome(action.reloadWebView)

          // 웹뷰가 더 뒤로 갈 수 있으면 웹뷰 뒤로가기, 아니면 네이티브 네비게이션 pop.
          is BridgeAction.NavigateBack -> if (controller.canGoBack) controller.goBack() else popBackStack()

          // 회원 탈퇴 → 로컬 토큰·세션 정리 후 로그인(랜딩) 화면으로 이동.
          is BridgeAction.Withdraw -> onWithdraw()

          is BridgeAction.Unhandled -> Unit
        }
      },
      modifier = Modifier.fillMaxSize(),
    )
    // 페이지 로딩 중에는 웹뷰 위에 shimmer 스켈레톤을 덮어 보여준다.
    if (controller.isLoading) {
      WebViewLoadingShimmer()
    }
    }
   }
   DebugWebViewBadge()
  }
}
