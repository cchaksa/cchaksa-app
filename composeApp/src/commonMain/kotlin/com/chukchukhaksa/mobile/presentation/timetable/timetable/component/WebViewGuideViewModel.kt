package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewHolder
import com.chukchukhaksa.mobile.common.designsystem.component.webview.webHomeUrl
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.auth.usecase.WithdrawUseCase
import com.chukchukhaksa.mobile.domain.webview.ExchangeStatus
import com.chukchukhaksa.mobile.domain.webview.ExchangeWebSessionUseCase
import com.chukchukhaksa.mobile.presentation.webview.BridgeAction
import com.chukchukhaksa.mobile.presentation.webview.HomeRedirectEventBus
import com.chukchukhaksa.mobile.presentation.webview.toAction
import com.chukchukhaksa.mobile.remote.auth.AuthEvent
import com.chukchukhaksa.mobile.remote.auth.AuthEventBus
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

class WebViewGuideViewModel(
  private val exchangeWebSession: ExchangeWebSessionUseCase,
  private val authEventBus: AuthEventBus,
  private val webViewHolder: WebViewHolder,
  private val homeRedirectEventBus: HomeRedirectEventBus,
  private val withdraw: WithdrawUseCase,
) : ViewModel() {

  val mviStore: MviStore<WebViewGuideState, WebViewGuideSideEffect> =
    mviStore(WebViewGuideState())

  private val currentHost: String = webHomeUrl.substringAfter("://").substringBefore("/")
  private val timeSource = TimeSource.Monotonic
  private var lastPushMark: TimeSource.Monotonic.ValueTimeMark? = null

  init {
    observeCookies()
    observeAuthEvents()
    observeHomeRedirect()
    refresh()
  }

  fun refresh() {
    viewModelScope.launch {
      val status = exchangeWebSession.refresh()
      mviStore.setState { copy(exchangeStatus = status) }
      when {
        status is ExchangeStatus.Failed400 -> {
          mviStore.postSideEffect(WebViewGuideSideEffect.NavigateToLogin)
        }

        status == ExchangeStatus.Loaded && !webViewHolder.isInitialLoaded() -> {
          webViewHolder.preload(webHomeUrl, exchangeWebSession.cookies.value)
        }
      }
    }
  }

  fun onBridgeMessage(message: BridgeMessage) {
    Napier.w(tag = "BridgeAction") { "raw message: $message" }
    when (val action = message.toAction(currentHost)) {
      is BridgeAction.NavigateWebView -> {
        val previous = mviStore.uiState.value.lastPushedUrl
        val mark = lastPushMark
        if (previous == action.absoluteUrl &&
          mark != null &&
          mark.elapsedNow().inWholeMilliseconds < DUPLICATE_PUSH_DEBOUNCE_MS
        ) {
          Napier.w(tag = "BridgeAction") {
            "Skipped duplicate push within ${mark.elapsedNow().inWholeMilliseconds}ms: ${action.absoluteUrl}"
          }
          return
        }
        lastPushMark = timeSource.markNow()
        mviStore.setState { copy(lastPushedUrl = action.absoluteUrl) }
        mviStore.postSideEffect(WebViewGuideSideEffect.NavigateWebView(absoluteUrl = action.absoluteUrl))
      }

      is BridgeAction.RedirectToHome -> homeRedirectEventBus.redirectToHome(action.reloadWebView)

      // 홈 탭은 루트라 네이티브 pop 대상이 없으므로, 컨트롤러를 가진 화면이 웹뷰 뒤로가기만 처리한다.
      is BridgeAction.NavigateBack -> mviStore.postSideEffect(WebViewGuideSideEffect.NavigateBack)

      // 회원 탈퇴 → 로컬 토큰·세션 정리 후 로그인(랜딩) 화면으로 이동.
      is BridgeAction.Withdraw -> viewModelScope.launch {
        withdraw()
        mviStore.postSideEffect(WebViewGuideSideEffect.NavigateToLogin)
      }

      is BridgeAction.ContentRendered -> mviStore.setState { copy(isContentRendered = true) }

      is BridgeAction.Unhandled -> Unit
    }
  }

  /**
   * 이미 살아있는 홈 웹뷰가 재로드 요청(예: 포털 연동 완료)을 받으면 직접 홀더를 리셋하고 다시 로드한다.
   * (신규 진입 시에는 App에서 홀더를 리셋한 뒤 init의 refresh()가 재로드를 담당한다.)
   */
  private fun observeHomeRedirect() {
    viewModelScope.launch {
      homeRedirectEventBus.events.collect { event ->
        if (event.reloadWebView) {
          webViewHolder.reset()
          // 재로드되는 페이지의 rendered 이벤트를 다시 기다리도록 초기화한다.
          mviStore.setState { copy(isContentRendered = false) }
          refresh()
        }
      }
    }
  }

  private fun observeCookies() {
    viewModelScope.launch {
      exchangeWebSession.cookies.collect { cookies ->
        mviStore.setState { copy(cookies = cookies.toImmutableList()) }
      }
    }
  }

  private fun observeAuthEvents() {
    viewModelScope.launch {
      authEventBus.events.collect { event ->
        if (event is AuthEvent.TokenExpired) {
          exchangeWebSession.clear()
          webViewHolder.reset()
          mviStore.setState {
            copy(
              exchangeStatus = ExchangeStatus.NotLoggedIn,
              cookies = persistentListOf(),
              isContentRendered = false,
            )
          }
          mviStore.postSideEffect(WebViewGuideSideEffect.NavigateToLogin)
        }
      }
    }
  }

}

data class WebViewGuideState(
  val exchangeStatus: ExchangeStatus = ExchangeStatus.Loading,
  val cookies: ImmutableList<WebViewCookie> = persistentListOf(),
  val lastPushedUrl: String? = null,
  // 웹의 rendered 브릿지 이벤트 수신 여부. 수신 전까지는 웹뷰 위에 shimmer를 덮는다.
  val isContentRendered: Boolean = false,
)

sealed interface WebViewGuideSideEffect {
  data object NavigateToLogin : WebViewGuideSideEffect
  data class NavigateWebView(val absoluteUrl: String) : WebViewGuideSideEffect
  data object NavigateBack : WebViewGuideSideEffect
}

private const val DUPLICATE_PUSH_DEBOUNCE_MS = 500L
