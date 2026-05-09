package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.designsystem.component.loading.LoadingScreen
import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import com.chukchukhaksa.mobile.common.designsystem.component.webview.CchWebView
import com.chukchukhaksa.mobile.common.designsystem.component.webview.rememberCchWebViewController
import com.chukchukhaksa.mobile.common.designsystem.component.webview.webHomeUrl
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.PlatformBackHandler
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.domain.webview.ExchangeStatus
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WebViewGuideScreen(
  navigateToLogin: () -> Unit = {},
  navigateWebView: (String) -> Unit = {},
  viewModel: WebViewGuideViewModel = koinViewModel(),
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

  viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
    when (sideEffect) {
      WebViewGuideSideEffect.NavigateToLogin -> navigateToLogin()
      is WebViewGuideSideEffect.NavigateWebView -> navigateWebView(sideEffect.absoluteUrl)
    }
  }

  WebViewGuideContent(
    state = uiState,
    onBridgeMessage = viewModel::onBridgeMessage,
  )
}

@Composable
private fun WebViewGuideContent(
  state: WebViewGuideState,
  onBridgeMessage: (BridgeMessage) -> Unit,
) {
  val controller = rememberCchWebViewController()
  PlatformBackHandler(enabled = controller.canGoBack) {
    controller.goBack()
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(White100)
      .windowInsetsPadding(WindowInsets.systemBars),
  ) {
    when (state.exchangeStatus) {
      ExchangeStatus.Loading -> LoadingScreen()
      ExchangeStatus.Failed400 -> Unit
      else -> CchWebView(
        url = webHomeUrl,
        controller = controller,
        cookies = state.cookies,
        onBridgeMessage = onBridgeMessage,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}
