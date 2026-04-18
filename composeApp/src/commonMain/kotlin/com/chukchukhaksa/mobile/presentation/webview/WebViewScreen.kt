package com.chukchukhaksa.mobile.presentation.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.CchAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.webview.CchWebView
import com.chukchukhaksa.mobile.common.designsystem.component.webview.CchWebViewController
import com.chukchukhaksa.mobile.common.designsystem.component.webview.rememberCchWebViewController
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.PlatformBackHandler

@Composable
fun WebViewRoute(
  url: String,
  popBackStack: () -> Unit,
) {
  val controller = rememberCchWebViewController()
  WebViewRouteContent(
    url = url,
    popBackStack = popBackStack,
    controller = controller,
  )
}

@Composable
private fun WebViewRouteContent(
  url: String,
  popBackStack: () -> Unit,
  controller: CchWebViewController,
) {
  PlatformBackHandler(enabled = controller.canGoBack) {
    controller.goBack()
  }
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(White100),
  ) {
    CchAppBarWithTitle(
      title = "웹뷰",
      onClickBackButton = popBackStack,
    )
    CchWebView(
      url = url,
      controller = controller,
      modifier = Modifier.fillMaxSize(),
    )
  }
}
