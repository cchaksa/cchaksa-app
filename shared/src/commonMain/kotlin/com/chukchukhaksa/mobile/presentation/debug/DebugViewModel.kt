package com.chukchukhaksa.mobile.presentation.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.data.portallink.debug.PortalLinkDebugSettings
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkStrategy
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkMode
import kotlinx.coroutines.launch

/** 디버그 빌드에서 기기를 흔들면 열리는 화면. 포털 연동을 앱 로직으로 할지 서버 로직으로 할지 고른다. */
class DebugViewModel(
  private val portalLinkDebugSettings: PortalLinkDebugSettings,
) : ViewModel() {
  val mviStore: MviStore<DebugState, DebugSideEffect> =
    mviStore(DebugState(portalLinkStrategy = portalLinkDebugSettings.strategy.value))

  init {
    viewModelScope.launch {
      portalLinkDebugSettings.strategy.collect { strategy ->
        mviStore.setState { copy(portalLinkStrategy = strategy) }
      }
    }
  }

  fun selectPortalLinkStrategy(strategy: PortalLinkStrategy) {
    portalLinkDebugSettings.setStrategy(strategy)
  }

  /** 연동 여부와 관계없이 포털 연동 화면을 연다. 연동된 계정도 첫 연동 화면에서 시험할 수 있다. */
  fun openPortalLink(mode: PortalLinkMode) {
    mviStore.postSideEffect(DebugSideEffect.NavigatePortalLink(mode))
  }

  fun popBackStack() {
    mviStore.postSideEffect(DebugSideEffect.PopBackStack)
  }
}
