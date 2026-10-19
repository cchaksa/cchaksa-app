package com.chukchukhaksa.mobile.presentation.debug

import com.chukchukhaksa.mobile.data.portallink.debug.PortalLinkDebugSettings
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DebugViewModelTest {

  private val settings = PortalLinkDebugSettings()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `selected strategy is shared with the repository settings`() {
    val viewModel = DebugViewModel(settings)
    assertEquals(PortalLinkStrategy.DEVICE_FIRST, viewModel.mviStore.uiState.value.portalLinkStrategy)

    viewModel.selectPortalLinkStrategy(PortalLinkStrategy.SERVER_ONLY)

    assertEquals(PortalLinkStrategy.SERVER_ONLY, settings.strategy.value)
    assertEquals(PortalLinkStrategy.SERVER_ONLY, viewModel.mviStore.uiState.value.portalLinkStrategy)
  }

  @Test
  fun `reopened screen shows the strategy chosen before`() {
    settings.setStrategy(PortalLinkStrategy.DEVICE_ONLY)

    val viewModel = DebugViewModel(settings)

    assertEquals(PortalLinkStrategy.DEVICE_ONLY, viewModel.mviStore.uiState.value.portalLinkStrategy)
  }
}
