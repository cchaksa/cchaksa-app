package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewHolder
import com.chukchukhaksa.mobile.common.designsystem.component.webview.webHomeUrl
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
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
) : ViewModel() {

  val mviStore: MviStore<WebViewGuideState, WebViewGuideSideEffect> =
    mviStore(WebViewGuideState())

  private val currentHost: String = webHomeUrl.substringAfter("://").substringBefore("/")
  private val timeSource = TimeSource.Monotonic
  private var lastPushMark: TimeSource.Monotonic.ValueTimeMark? = null

  init {
    observeCookies()
    observeAuthEvents()
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

      is BridgeAction.RedirectToHome -> homeRedirectEventBus.redirectToHome()

      is BridgeAction.Unhandled -> Unit
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
)

sealed interface WebViewGuideSideEffect {
  data object NavigateToLogin : WebViewGuideSideEffect
  data class NavigateWebView(val absoluteUrl: String) : WebViewGuideSideEffect
}

private const val DUPLICATE_PUSH_DEBOUNCE_MS = 500L
