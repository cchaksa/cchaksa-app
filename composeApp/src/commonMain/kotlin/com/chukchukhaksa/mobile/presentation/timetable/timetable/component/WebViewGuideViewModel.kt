package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.designsystem.component.webview.BridgeMessage
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewCookie
import com.chukchukhaksa.mobile.common.designsystem.component.webview.webHomeUrl
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.webview.ExchangeStatus
import com.chukchukhaksa.mobile.domain.webview.ExchangeWebSessionUseCase
import com.chukchukhaksa.mobile.presentation.webview.BridgeAction
import com.chukchukhaksa.mobile.presentation.webview.toAction
import com.chukchukhaksa.mobile.remote.auth.AuthEvent
import com.chukchukhaksa.mobile.remote.auth.AuthEventBus
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

class WebViewGuideViewModel(
  private val exchangeWebSession: ExchangeWebSessionUseCase,
  private val authEventBus: AuthEventBus,
) : ViewModel() {

  val mviStore: MviStore<WebViewGuideState, WebViewGuideSideEffect> =
    mviStore(WebViewGuideState())

  private val currentHost: String = webHomeUrl.substringAfter("://").substringBefore("/")

  init {
    observeCookies()
    observeAuthEvents()
    refresh()
  }

  fun refresh() {
    viewModelScope.launch {
      val status = exchangeWebSession.refresh()
      mviStore.setState { copy(exchangeStatus = status) }
      if (status is ExchangeStatus.Failed400) {
        mviStore.postSideEffect(WebViewGuideSideEffect.NavigateToLogin)
      }
    }
  }

  fun onBridgeMessage(message: BridgeMessage) {
    Napier.w(tag = "BridgeAction") { "raw message: $message" }
    when (val action = message.toAction(currentHost)) {
      is BridgeAction.NavigateWebView -> {
        val previous = mviStore.uiState.value.lastPushedUrl
        if (previous == action.absoluteUrl) {
          Napier.w(tag = "BridgeAction") { "Skipped duplicate push: ${action.absoluteUrl}" }
          return
        }
        mviStore.setState { copy(lastPushedUrl = action.absoluteUrl) }
        mviStore.postSideEffect(WebViewGuideSideEffect.NavigateWebView(absoluteUrl = action.absoluteUrl))
      }

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
