package com.chukchukhaksa.mobile.presentation.portallink

import androidx.lifecycle.SavedStateHandle
import com.chukchukhaksa.mobile.common.analytics.AnalyticsClient
import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkException
import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkFailure
import com.chukchukhaksa.mobile.common.model.portallink.PortalScrapeUpload
import com.chukchukhaksa.mobile.common.model.portalscrape.portalScrapeResultFixture
import com.chukchukhaksa.mobile.data.portallink.repository.PortalLinkRepository
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkMode
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkRouteSpec
import com.chukchukhaksa.mobile.presentation.webview.HomeRedirectEvent
import com.chukchukhaksa.mobile.presentation.webview.HomeRedirectEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PortalLinkViewModelTest {

  private val repository = FakePortalLinkRepository()
  private val homeRedirectEventBus = HomeRedirectEventBus()
  private val analytics = FakeAnalyticsClient()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun viewModel(mode: PortalLinkMode) = PortalLinkViewModel(
    savedStateHandle = SavedStateHandle(mapOf(PortalLinkRouteSpec.MODE_ARG to mode.name)),
    portalLinkRepository = repository,
    homeRedirectEventBus = homeRedirectEventBus,
    analyticsClient = analytics,
  )

  private fun PortalLinkViewModel.fillForm() {
    updateStudentId("2023 1234")
    updatePassword("pw")
  }

  @Test
  fun `credential failure returns to the form with the student id kept`() {
    repository.linkFailure = PortalLinkFailure.Credential("PORTAL_AUTH_FAILED")
    val viewModel = viewModel(PortalLinkMode.LINK)
    viewModel.fillForm()

    viewModel.submit()

    val state = viewModel.mviStore.uiState.value
    assertEquals(listOf("20231234" to "pw"), repository.linkCalls)
    assertEquals(PortalLinkStep.FORM, state.step)
    assertEquals("20231234", state.studentId)
    assertEquals("", state.password)
    assertEquals("아이디나 비밀번호가 일치하지 않습니다.\n학교 홈페이지에서 확인해주세요.", state.formErrorMessage)
    assertEquals(
      listOf(
        "univ_sync_login_view" to emptyMap(),
        "univ_sync_btn_tap" to emptyMap(),
        "univ_sync_loading_view" to emptyMap(),
        "univ_sync_fail" to mapOf("reason" to "PORTAL_AUTH_FAILED"),
      ),
      analytics.events,
    )
  }

  @Test
  fun `retry after an upload failure uploads the same result without scraping again`() {
    val upload = PortalScrapeUpload(portalScrapeResultFixture(), idempotencyKey = "key-1")
    repository.linkFailure = PortalLinkFailure.UploadFailed(upload)
    val viewModel = viewModel(PortalLinkMode.RESYNC)
    viewModel.fillForm()
    viewModel.submit()
    assertEquals(PortalLinkStep.FAILURE, viewModel.mviStore.uiState.value.step)

    viewModel.retry()

    assertEquals(1, repository.linkCalls.size)
    assertSame(upload, repository.uploads.single())
    assertEquals(HomeRedirectEvent(reloadWebView = true), homeRedirectEventBus.consumePending())
    assertTrue(analytics.events.contains("univ_resync_complete" to emptyMap()))
  }

  @Test
  fun `retry after other failures goes back to the form`() {
    repository.linkFailure = PortalLinkFailure.Ineligible("S04")
    val viewModel = viewModel(PortalLinkMode.LINK)
    viewModel.fillForm()
    viewModel.submit()
    assertEquals(
      "이미 포털 연동된 학생 정보가 존재합니다.\n다른 계정으로 로그인했는지 확인해주세요.",
      viewModel.mviStore.uiState.value.failureMessage,
    )

    viewModel.retry()

    val state = viewModel.mviStore.uiState.value
    assertEquals(PortalLinkStep.FORM, state.step)
    assertEquals("20231234", state.studentId)
    assertEquals("", state.password)
    assertTrue(repository.uploads.isEmpty())
  }

  @Test
  fun `success reloads the home web view`() {
    val viewModel = viewModel(PortalLinkMode.LINK)
    viewModel.fillForm()

    viewModel.submit()

    assertEquals(HomeRedirectEvent(reloadWebView = true), homeRedirectEventBus.consumePending())
  }

  @Test
  fun `continuing without link goes home without reloading`() {
    val viewModel = viewModel(PortalLinkMode.LINK)
    viewModel.showUnavailableDialog()

    viewModel.continueWithoutLink()

    assertEquals(false, viewModel.mviStore.uiState.value.isUnavailableDialogVisible)
    assertEquals(HomeRedirectEvent(reloadWebView = false), homeRedirectEventBus.consumePending())
  }

  @Test
  fun `submit is ignored until both fields are filled`() {
    val viewModel = viewModel(PortalLinkMode.LINK)
    viewModel.updateStudentId("20231234")
    viewModel.submit()

    // 공백만 있는 비밀번호는 스크래퍼가 INVALID_PAYLOAD로 거부하므로 보내지 않는다.
    viewModel.updatePassword("   ")
    viewModel.submit()

    assertTrue(repository.linkCalls.isEmpty())
    assertNull(homeRedirectEventBus.consumePending())
  }

  private class FakePortalLinkRepository : PortalLinkRepository {
    var linkFailure: PortalLinkFailure? = null
    val linkCalls = mutableListOf<Pair<String, String>>()
    val uploads = mutableListOf<PortalScrapeUpload>()

    override suspend fun link(studentId: String, password: String) {
      linkCalls += studentId to password
      linkFailure?.let { throw PortalLinkException(it) }
    }

    override suspend fun upload(upload: PortalScrapeUpload) {
      uploads += upload
    }
  }

  private class FakeAnalyticsClient : AnalyticsClient {
    val events = mutableListOf<Pair<String, Map<String, Any?>>>()

    override fun setUserId(userId: String?) = Unit

    override fun track(eventType: String, properties: Map<String, Any?>) {
      events += eventType to properties
    }

    override fun setUserProperties(properties: Map<String, Any?>) = Unit
  }
}
