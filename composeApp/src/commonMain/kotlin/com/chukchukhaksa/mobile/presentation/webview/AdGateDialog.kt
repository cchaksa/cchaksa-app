package com.chukchukhaksa.mobile.presentation.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chukchukhaksa.mobile.common.ad.AdShowResult
import com.chukchukhaksa.mobile.common.designsystem.component.dialog.CchDialog
import com.chukchukhaksa.mobile.common.designsystem.component.loading.LoadingScreen
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100

// 전면 광고 로드·표시 실패 시 이동 직전에 노출하는 안내 토스트 문구(D5).
private const val AD_UNAVAILABLE_MESSAGE = "지금은 광고가 없어 바로 학업 정보 업데이트 화면으로 이동합니다."

/**
 * 광고 게이트 경로 진입 시 "광고가 노출됩니다"를 고지하는 확인/취소 다이얼로그.
 * 디자인 시스템 [CchDialog](2버튼 구성)를 래핑한다.
 *
 * - [onConfirm]: 전면 광고 표시 → 이동 흐름을 시작한다.
 * - [onCancel]: 광고·이동 없이 현재 화면을 유지한다(다이얼로그 닫기).
 */
@Composable
fun AdGateDialog(
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
) {
  CchDialog(
    headerText = "광고가 노출됩니다",
    bodyText = "확인을 누르면 광고가 표시된 후 이동합니다.",
    confirmButtonText = "확인",
    dismissButtonText = "취소",
    onDismissRequest = onCancel,
    onClickConfirm = onConfirm,
    onClickDismiss = onCancel,
  )
}

/**
 * 광고 게이트 확인 후 전면 광고가 로드·표시되는 동안 덮는 로딩 오버레이.
 * 스크림 위에 중앙 스피너([LoadingScreen])를 올려 표시하고, 입력은 [LoadingScreen]이 소비해 차단한다.
 * 광고가 풀스크린으로 뜨면 그 아래에 가려지고, 로드 실패·광고 종료 후 이동이 일어나면 사라진다.
 */
@Composable
fun AdLoadingOverlay() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Black100.copy(alpha = 0.2f)),
  ) {
    LoadingScreen()
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
