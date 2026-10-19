package com.chukchukhaksa.mobile.presentation.debug

import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkStrategy
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkMode

data class DebugState(
  val portalLinkStrategy: PortalLinkStrategy = PortalLinkStrategy.DEVICE_FIRST,
)

sealed interface DebugSideEffect {
  data object PopBackStack : DebugSideEffect
  data class NavigatePortalLink(val mode: PortalLinkMode) : DebugSideEffect
}
