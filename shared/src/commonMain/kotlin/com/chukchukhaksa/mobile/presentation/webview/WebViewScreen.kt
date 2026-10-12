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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.ad.AdFailureReason
import com.chukchukhaksa.mobile.common.ad.AdManager
import com.chukchukhaksa.mobile.common.ad.AdShowResult
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import kotlin.time.TimeSource

private const val DUPLICATE_PUSH_DEBOUNCE_MS = 500L

// 웹에서 rendered 브릿지 이벤트가 오지 않아도 shimmer를 강제로 숨기는 최대 대기 시간.
private const val RENDERED_EVENT_TIMEOUT_MS = 5_000L

@Composable
fun WebViewRoute(
  url: String,
  popBackStack: () -> Unit,
  onNavigateWebView: (String) -> Unit,
  navigateToLanding: () -> Unit,
  onShowToast: (String) -> Unit,
) {
  val controller = rememberCchWebViewController()
  val exchangeWebSession: ExchangeWebSessionUseCase = koinInject()
  val homeRedirectEventBus: HomeRedirectEventBus = koinInject()
  val withdraw: WithdrawUseCase = koinInject()
  // AdManager 구현체는 Phase 2(Android)/Phase 3(iOS)에서 platformModule로 주입된다.
  // 미주입 상태에서 eager 주입(koinInject<AdManager>())하면 모든 웹뷰 진입이 크래시하므로(G5 회귀),
  // Koin 인스턴스만 확보해 게이트 경로 흐름에서만 지연 해석한다.
  val koin = getKoin()
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
    onShowToast = onShowToast,
    // 구현체 미주입(Phase 2/3 전)이면 NotReady 실패로 환원해 토스트 후 이동(크래시 방지, D5와 정합).
    onShowInterstitial = {
      koin.getOrNull<AdManager>()?.showInterstitial() ?: AdShowResult.Failed(AdFailureReason.NotReady)
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
  onShowToast: (String) -> Unit,
  onShowInterstitial: suspend () -> AdShowResult,
) {
  PlatformBackHandler(enabled = controller.canGoBack) {
    controller.goBack()
  }
  val currentHost = remember(url) {
    url.substringAfter("://").substringBefore("/")
  }
  var lastPushedUrl by rememberSaveable { mutableStateOf<String?>(null) }
  val lastPushMarkHolder = remember { object { var mark: TimeSource.Monotonic.ValueTimeMark? = null } }

  // 중복 push 디바운스를 통과시켜 웹뷰를 이동시킨다.
  // 일반 navigate 분기와 광고 게이트 확인 후의 최종 이동이 동일한 가드를 공유한다.
  val pushWebView: (String) -> Unit = { absoluteUrl ->
    val mark = lastPushMarkHolder.mark
    val elapsedMs = mark?.elapsedNow()?.inWholeMilliseconds ?: Long.MAX_VALUE
    if (lastPushedUrl == absoluteUrl && elapsedMs < DUPLICATE_PUSH_DEBOUNCE_MS) {
      Napier.w(tag = "BridgeAction") {
        "Skipped duplicate push within ${elapsedMs}ms: $absoluteUrl"
      }
    } else {
      lastPushedUrl = absoluteUrl
      lastPushMarkHolder.mark = TimeSource.Monotonic.markNow()
      onNavigateWebView(absoluteUrl)
    }
  }

  // 광고 게이트 다이얼로그 대상 URL. null이면 다이얼로그를 표시하지 않는다.
  val adScope = rememberCoroutineScope()
  var pendingAdNavUrl by rememberSaveable { mutableStateOf<String?>(null) }
  // 확인 후 전면 광고가 로드·표시되는 동안 로딩 오버레이를 덮는다.
  var isAdLoading by remember { mutableStateOf(false) }

  // 웹의 rendered 브릿지 이벤트가 올 때까지 shimmer를 유지하고,
  // 타임아웃 안에 이벤트가 오지 않으면 그냥 숨긴다.
  var showShimmer by remember { mutableStateOf(true) }
  LaunchedEffect(Unit) {
    delay(RENDERED_EVENT_TIMEOUT_MS)
    showShimmer = false
  }

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
          is BridgeAction.NavigateWebView -> pushWebView(action.absoluteUrl)

          // 광고 게이트 경로: 즉시 이동하지 않고 "광고가 노출됩니다" 다이얼로그를 띄운다.
          // 전면 광고 로드는 확인 시점에 시작한다(사전 로드 없음 — 취소 시 낭비 요청 방지).
          is BridgeAction.NavigateWebViewWithAd -> {
            pendingAdNavUrl = action.absoluteUrl
          }

          is BridgeAction.RedirectToHome -> onRedirectToHome(action.reloadWebView)

          // 웹뷰가 더 뒤로 갈 수 있으면 웹뷰 뒤로가기, 아니면 네이티브 네비게이션 pop.
          is BridgeAction.NavigateBack -> if (controller.canGoBack) controller.goBack() else popBackStack()

          // 회원 탈퇴 → 로컬 토큰·세션 정리 후 로그인(랜딩) 화면으로 이동.
          is BridgeAction.Withdraw -> onWithdraw()

          is BridgeAction.ContentRendered -> showShimmer = false

          is BridgeAction.Unhandled -> Unit
        }
      },
      modifier = Modifier.fillMaxSize(),
    )
    // rendered 브릿지 이벤트가 오기 전까지 웹뷰 위에 shimmer 스켈레톤을 덮어 보여준다.
    if (showShimmer) {
      WebViewLoadingShimmer()
    }
    }
   }
   DebugWebViewBadge()

   // 광고 게이트 다이얼로그. 확인 시 전면 광고를 표시한 뒤 결과와 무관하게 이동하고(Failed면 안내 토스트 후 이동),
   // 취소 시 광고·이동 없이 현재 화면을 유지한다. 최종 이동은 일반 navigate와 동일한 디바운스 가드를 통과한다.
   val gateUrl = pendingAdNavUrl
   if (gateUrl != null) {
     AdGateDialog(
       onConfirm = {
         pendingAdNavUrl = null
         isAdLoading = true
         adScope.launch {
           try {
             showAdGateInterstitialThenNavigate(
               showInterstitial = onShowInterstitial,
               onShowToast = onShowToast,
               navigate = { pushWebView(gateUrl) },
             )
           } finally {
             isAdLoading = false
           }
         }
       },
       onCancel = { pendingAdNavUrl = null },
     )
   }

   // 확인 후 광고 로드·표시 대기 동안 웹뷰 위를 로딩 오버레이로 덮는다.
   if (isAdLoading) {
     AdLoadingOverlay()
   }
  }
}
