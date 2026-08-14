package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.ad.AdFailureReason
import com.chukchukhaksa.mobile.common.ad.AdManager
import com.chukchukhaksa.mobile.common.ad.AdShowResult
import com.chukchukhaksa.mobile.common.designsystem.component.loading.WebViewLoadingShimmer
import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import com.chukchukhaksa.mobile.common.designsystem.component.webview.CchHomeWebView
import com.chukchukhaksa.mobile.common.designsystem.component.webview.DebugWebViewBadge
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewHolder
import com.chukchukhaksa.mobile.common.designsystem.component.webview.CchWebViewController
import com.chukchukhaksa.mobile.common.designsystem.component.webview.rememberCchWebViewController
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.PlatformBackHandler
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.domain.webview.ExchangeStatus
import com.chukchukhaksa.mobile.presentation.webview.AdGateDialog
import com.chukchukhaksa.mobile.presentation.webview.AdLoadingOverlay
import com.chukchukhaksa.mobile.presentation.webview.showAdGateInterstitialThenNavigate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// 웹에서 rendered 브릿지 이벤트가 오지 않아도 shimmer를 강제로 숨기는 최대 대기 시간.
private const val RENDERED_EVENT_TIMEOUT_MS = 5_000L

@Composable
fun WebViewGuideScreen(
  navigateToLogin: () -> Unit = {},
  navigateWebView: (String) -> Unit = {},
  onShowToast: (String) -> Unit = {},
  viewModel: WebViewGuideViewModel = koinViewModel(),
  holder: WebViewHolder = koinInject(),
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
  val controller = rememberCchWebViewController()
  // AdManager 구현체는 Phase 2/3에서 주입된다. 미주입 상태에서 VM 생성자 주입 시 홈 탭이 크래시하므로,
  // 화면에서 Koin 인스턴스만 확보해 게이트 흐름에서만 지연 해석한다(WebViewScreen과 동일).
  val koin = getKoin()
  val scope = rememberCoroutineScope()

  viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
    when (sideEffect) {
      WebViewGuideSideEffect.NavigateToLogin -> navigateToLogin()
      is WebViewGuideSideEffect.NavigateWebView -> navigateWebView(sideEffect.absoluteUrl)
      // 홈 탭은 루트라 네이티브 pop 대상이 없으므로, 웹뷰가 뒤로 갈 수 있을 때만 웹뷰 뒤로가기.
      WebViewGuideSideEffect.NavigateBack -> if (controller.canGoBack) controller.goBack()
    }
  }

  // 확인 후 전면 광고가 로드·표시되는 동안 로딩 오버레이를 덮는다.
  var isAdLoading by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxSize()) {
    WebViewGuideContent(
      state = uiState,
      holder = holder,
      controller = controller,
      onBridgeMessage = viewModel::onBridgeMessage,
    )

    // 확인 후 광고 로드·표시 대기 동안 홈 웹뷰 위를 로딩 오버레이로 덮는다.
    if (isAdLoading) {
      AdLoadingOverlay()
    }
  }

  // 광고 게이트 다이얼로그. 확인 시 전면 광고를 표시한 뒤 결과와 무관하게 이동하고(Failed면 안내 토스트 후 이동),
  // 취소 시 광고·이동 없이 현재 화면을 유지한다. 최종 이동은 일반 navigate와 동일한 디바운스 가드를 통과한다.
  val gateUrl = uiState.pendingAdNavUrl
  if (gateUrl != null) {
    AdGateDialog(
      onConfirm = {
        viewModel.dismissAdGate()
        isAdLoading = true
        scope.launch {
          try {
            showAdGateInterstitialThenNavigate(
              // 구현체 미주입(Phase 2/3 전)이면 NotReady 실패로 환원해 토스트 후 이동(크래시 방지, D5와 정합).
              showInterstitial = {
                koin.getOrNull<AdManager>()?.showInterstitial() ?: AdShowResult.Failed(AdFailureReason.NotReady)
              },
              onShowToast = onShowToast,
              navigate = { viewModel.navigateAfterAdGate(gateUrl) },
            )
          } finally {
            isAdLoading = false
          }
        }
      },
      onCancel = { viewModel.dismissAdGate() },
    )
  }
}

@Composable
private fun WebViewGuideContent(
  state: WebViewGuideState,
  holder: WebViewHolder,
  controller: CchWebViewController,
  onBridgeMessage: (BridgeMessage) -> Unit,
) {
  PlatformBackHandler(enabled = controller.canGoBack) {
    controller.goBack()
  }

  // 세션 교환이 끝나(또는 프리로드되어) 웹뷰를 화면에 올릴 수 있는 상태인지.
  val webViewVisible = when (state.exchangeStatus) {
    ExchangeStatus.Failed400 -> false
    ExchangeStatus.Loading -> holder.isInitialLoaded()
    else -> true
  }

  // 웹뷰가 보이더라도 rendered 브릿지 이벤트가 올 때까지 shimmer를 덮어두고,
  // 타임아웃 안에 이벤트가 오지 않으면 그냥 숨긴다.
  var renderTimedOut by remember { mutableStateOf(false) }
  LaunchedEffect(webViewVisible, state.isContentRendered) {
    if (webViewVisible && !state.isContentRendered) {
      renderTimedOut = false
      delay(RENDERED_EVENT_TIMEOUT_MS)
      renderTimedOut = true
    }
  }
  val showShimmer = when {
    state.exchangeStatus == ExchangeStatus.Failed400 -> false
    !webViewVisible -> true
    else -> !state.isContentRendered && !renderTimedOut
  }

  Box(modifier = Modifier.fillMaxSize()) {
   Column(
    modifier = Modifier
      .fillMaxSize()
      .background(White100)
   ) {
    Box(modifier = Modifier.fillMaxSize()) {
      if (webViewVisible) {
        CchHomeWebView(
          holder = holder,
          controller = controller,
          cookies = state.cookies,
          onBridgeMessage = onBridgeMessage,
          modifier = Modifier.fillMaxSize(),
        )
      }
      if (showShimmer) {
        // 홈 웹뷰는 edge-to-edge로 그려지므로 shimmer도 statusBar 패딩 없이 전체를 덮는다.
        WebViewLoadingShimmer(applyStatusBarPadding = false)
      }
    }
   }
   DebugWebViewBadge()
  }
}
