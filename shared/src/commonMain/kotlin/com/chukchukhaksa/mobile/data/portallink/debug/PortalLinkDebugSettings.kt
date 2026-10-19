package com.chukchukhaksa.mobile.data.portallink.debug

import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 디버그 화면에서 고른 포털 연동 경로. 디버그 화면은 디버그 빌드에만 있으므로 출시 빌드에서는 기본값 그대로다.
 * 저장하지 않아서 앱을 다시 시작하면 기본값으로 돌아간다.
 */
class PortalLinkDebugSettings {
  private val _strategy = MutableStateFlow(PortalLinkStrategy.DEVICE_FIRST)
  val strategy: StateFlow<PortalLinkStrategy> = _strategy.asStateFlow()

  fun setStrategy(strategy: PortalLinkStrategy) {
    _strategy.value = strategy
  }
}
