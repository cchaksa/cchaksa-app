package com.chukchukhaksa.mobile.presentation.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.chukchukhaksa.mobile.common.ad.AdShowResult
import com.chukchukhaksa.mobile.common.analytics.AnalyticsClient
import com.chukchukhaksa.mobile.common.designsystem.component.dialog.CchDialog
import com.chukchukhaksa.mobile.common.designsystem.component.loading.LoadingScreen
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import org.koin.compose.koinInject

// 전면 광고 로드·표시 실패 시 이동 직전에 노출하는 안내 토스트 문구(D5).
private const val AD_UNAVAILABLE_MESSAGE = "지금은 광고가 없어 바로 학업 정보 업데이트 화면으로 이동합니다."

// 광고 안내 팝업이 노출될 때 Amplitude로 전송하는 이벤트 이름.
private const val AD_CONFIRM_POPUP_EVENT = "ad_confirm_popup"

/**
 * 광고 게이트 경로 진입 시 광고 재생 후 이동을 고지하는 확인/취소 다이얼로그.
 * 디자인 시스템 [CchDialog](2버튼 구성)를 래핑한다.
 *
 * - [onConfirm]: 전면 광고 표시 → 이동 흐름을 시작한다.
 * - [onCancel]: 광고·이동 없이 현재 화면을 유지한다(다이얼로그 닫기).
 *
 * 다이얼로그가 컴포지션에 진입(= 팝업 노출)할 때마다 [AD_CONFIRM_POPUP_EVENT]를 1회 전송한다.
 * 게이트를 띄우는 두 경로(보조 웹뷰 [WebViewRoute]·홈 탭 웹뷰 WebViewGuideScreen)가 이 컴포저블을
 * 공유하므로, 노출 계측을 여기서 처리하면 양쪽 모두 누락 없이 커버된다.
 */
@Composable
fun AdGateDialog(
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
  analyticsClient: AnalyticsClient = koinInject(),
) {
  LaunchedEffect(Unit) {
    analyticsClient.track(AD_CONFIRM_POPUP_EVENT)
  }
  CchDialog(
    headerText = "안내",
    bodyText = "척척학사를 위한 짧은 광고가 재생된 후,\n학업 정보 업데이트 화면으로 이동합니다.",
    confirmButtonText = "확인",
    dismissButtonText = "취소",
    onDismissRequest = onCancel,
    onClickConfirm = onConfirm,
    onClickDismiss = onCancel,
  )
}

/**
 * 광고 게이트 확인 후 전면 광고가 로드·표시되는 동안 덮는 로딩 오버레이.
 * [Dialog]로 감싸 별도 윈도우에 그려지므로, 호스트 화면의 appbar·시스템 바까지 포함한
 * 전체 화면을 스크림으로 덮는다(웹뷰 content 영역만 차지하던 Box 방식의 한계 해소).
 * 스크림 위에 중앙 스피너([LoadingScreen])를 올리고, 입력은 [LoadingScreen]이 소비해 차단한다.
 * 광고가 풀스크린으로 뜨면 그 위에 가려지고, 로드 실패·광고 종료 후 이동이 일어나면 사라진다.
 */
@Composable
fun AdLoadingOverlay() {
  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      dismissOnBackPress = false,
      dismissOnClickOutside = false,
    ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize(),
    ) {
      LoadingScreen()
    }
  }
}

/**
 * 광고 게이트 확인 후 공통 흐름: 전면 광고를 표시하고 결과와 무관하게 [navigate]로 이동한다.
 * 결과가 [AdShowResult.Failed]이면 이동 직전에 안내 토스트를 띄운다(Dismissed·취소 시 미노출, D5).
 * 보조 웹뷰([WebViewRoute])와 홈 탭 웹뷰(WebViewGuideScreen)가 동일 규약을 공유한다.
 */
internal suspend fun showAdGateInterstitialThenNavigate(
  showInterstitial: suspend () -> AdShowResult,
  onShowToast: (String) -> Unit,
  navigate: () -> Unit,
) {
  val result = showInterstitial()
  if (result is AdShowResult.Failed) {
    onShowToast(AD_UNAVAILABLE_MESSAGE)
  }
  navigate()
}
