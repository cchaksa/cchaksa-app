package com.chukchukhaksa.mobile.presentation.portallink

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.analytics.AnalyticsClient
import com.chukchukhaksa.mobile.common.extension.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkException
import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkFailure
import com.chukchukhaksa.mobile.common.model.portallink.PortalScrapeUpload
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.data.portallink.repository.PortalLinkRepository
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkMode
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkRouteSpec
import com.chukchukhaksa.mobile.presentation.webview.HomeRedirectEventBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** 포털 연동(첫 연동·학업 정보 업데이트) 화면. 입력한 계정 정보는 화면을 벗어나면 사라지고 저장하지 않는다. */
class PortalLinkViewModel(
  savedStateHandle: SavedStateHandle,
  private val portalLinkRepository: PortalLinkRepository,
  private val homeRedirectEventBus: HomeRedirectEventBus,
  private val analyticsClient: AnalyticsClient,
) : ViewModel() {
  private val mode = PortalLinkMode.valueOf(savedStateHandle.get<String>(PortalLinkRouteSpec.MODE_ARG)!!)
  private val events = mode.analyticsEvents()

  val mviStore = mviStore<PortalLinkState, PortalLinkSideEffect>(PortalLinkState(mode = mode))

  /** 저장됐는지 확인하지 못한 업로드. 실패 화면에서 다시 시도하면 스크래핑 없이 같은 결과를 같은 키로 다시 올린다. */
  private var pendingUpload: PortalScrapeUpload? = null
  private var linkJob: Job? = null

  init {
    analyticsClient.track(events.formView)
  }

  fun updateStudentId(studentId: String) {
    mviStore.setState { copy(studentId = studentId.filterNot(Char::isWhitespace)) }
  }

  fun updatePassword(password: String) {
    mviStore.setState { copy(password = password) }
  }

  fun submit() {
    val state = mviStore.uiState.value
    if (!state.canSubmit) return
    analyticsClient.track(events.submit)
    runLink { portalLinkRepository.link(state.studentId, state.password) }
  }

  /** 실패 화면의 "다시 시도하기". 업로드가 일시적으로 실패했으면 다시 올리고, 아니면 입력 폼으로 돌아간다. */
  fun retry() {
    val upload = pendingUpload
    if (upload != null) {
      runLink { portalLinkRepository.upload(upload) }
    } else {
      // 웹처럼 학번은 남기고 비밀번호만 비운 폼으로 돌아간다.
      mviStore.setState { copy(step = PortalLinkStep.FORM, password = "", formErrorMessage = null) }
    }
  }

  fun showUnavailableDialog() {
    mviStore.setState { copy(isUnavailableDialogVisible = true) }
  }

  fun hideUnavailableDialog() {
    mviStore.setState { copy(isUnavailableDialogVisible = false) }
  }

  /** 학교 연동 없이 시간표 기능만 쓰기로 한 경우. 웹의 redirectToHome과 같이 홈으로 보낸다. */
  fun continueWithoutLink() {
    hideUnavailableDialog()
    homeRedirectEventBus.redirectToHome(reloadWebView = false)
  }

  fun popBackStack() {
    mviStore.postSideEffect(PortalLinkSideEffect.PopBackStack)
  }

  private fun runLink(block: suspend () -> Unit) {
    if (linkJob?.isActive == true) return
    mviStore.setState { copy(step = PortalLinkStep.LOADING, formErrorMessage = null) }
    analyticsClient.track(events.loadingView)
    linkJob = viewModelScope.launch {
      runCatchingIgnoreCancelled { block() }
        .onSuccess { onLinked() }
        .onFailure { onLinkFailed(it) }
    }
  }

  private fun onLinked() {
    pendingUpload = null
    events.complete?.let(analyticsClient::track)
    // 웹의 done:portal-link와 같이 홈 탭(웹뷰)을 다시 불러와 연동 결과를 보여 준다.
    homeRedirectEventBus.redirectToHome(reloadWebView = true)
  }

  private fun onLinkFailed(throwable: Throwable) {
    val failure = (throwable as? PortalLinkException)?.failure
      ?: PortalLinkFailure.System(PortalLinkFailure.UNKNOWN_CODE)
    events.fail?.let { analyticsClient.track(it, mapOf("reason" to failure.code)) }
    pendingUpload = (failure as? PortalLinkFailure.UploadFailed)?.upload
    mviStore.setState {
      if (failure is PortalLinkFailure.Credential) {
        copy(step = PortalLinkStep.FORM, password = "", formErrorMessage = failure.toMessage())
      } else {
        copy(step = PortalLinkStep.FAILURE, failureMessage = failure.toMessage())
      }
    }
  }
}

/** 웹이 같은 화면에서 보내던 분석 이벤트. 화면을 옮겨도 퍼널이 끊기지 않도록 이름을 그대로 쓴다. */
private class PortalLinkAnalyticsEvents(
  val formView: String,
  val submit: String,
  val loadingView: String,
  val complete: String?,
  val fail: String?,
)

private fun PortalLinkMode.analyticsEvents(): PortalLinkAnalyticsEvents = when (this) {
  PortalLinkMode.LINK -> PortalLinkAnalyticsEvents(
    formView = "univ_sync_login_view",
    submit = "univ_sync_btn_tap",
    loadingView = "univ_sync_loading_view",
    complete = null,
    fail = "univ_sync_fail",
  )

  PortalLinkMode.RESYNC -> PortalLinkAnalyticsEvents(
    formView = "univ_resync_login_view",
    submit = "univ_resync_btn_tap",
    loadingView = "univ_resync_loading_view",
    complete = "univ_resync_complete",
    fail = null,
  )
}
