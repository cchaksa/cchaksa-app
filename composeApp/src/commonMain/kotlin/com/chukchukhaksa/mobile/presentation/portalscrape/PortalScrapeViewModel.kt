package com.chukchukhaksa.mobile.presentation.portalscrape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeException
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.portalscrape.usecase.ScrapePortalUseCase
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

/** 디버그용 포털 스크래핑 테스트 화면. 입력한 계정 정보는 화면을 벗어나면 사라지고 저장하지 않는다. */
class PortalScrapeViewModel(
  private val scrapePortalUseCase: ScrapePortalUseCase,
) : ViewModel() {
  val mviStore = mviStore<PortalScrapeState, PortalScrapeSideEffect>(PortalScrapeState())

  fun updateStudentId(studentId: String) {
    mviStore.setState { copy(studentId = studentId.filterNot(Char::isWhitespace)) }
  }

  fun updatePassword(password: String) {
    mviStore.setState { copy(password = password) }
  }

  fun togglePasswordVisible() {
    mviStore.setState { copy(isPasswordVisible = !isPasswordVisible) }
  }

  fun startScrape() {
    val state = mviStore.uiState.value
    if (!state.canStart) return

    mviStore.setState { copy(isRunning = true, steps = initialStepStates(), result = null, error = null) }
    viewModelScope.launch {
      scrapePortalUseCase(state.studentId, state.password, onProgress = ::applyProgress)
        .onSuccess { result ->
          mviStore.setState { copy(isRunning = false, result = result) }
        }
        .onFailure { throwable ->
          mviStore.setState {
            copy(
              isRunning = false,
              error = throwable.toErrorState(),
              steps = steps.map {
                if (it.status == PortalScrapeStepStatus.RUNNING) it.copy(status = PortalScrapeStepStatus.INTERRUPTED) else it
              }.toPersistentList(),
            )
          }
        }
    }
  }

  fun copyRawJson() {
    val result = mviStore.uiState.value.result ?: return
    mviStore.postSideEffect(PortalScrapeSideEffect.CopyRawJson(result.rawJson))
  }

  fun popBackStack() {
    mviStore.postSideEffect(PortalScrapeSideEffect.PopBackStack)
  }

  private fun applyProgress(progress: PortalScrapeProgress) {
    mviStore.setState {
      copy(steps = steps.map { if (it.step == progress.step) it.applied(progress) else it }.toPersistentList())
    }
  }
}

private fun PortalScrapeStepState.applied(progress: PortalScrapeProgress): PortalScrapeStepState = when (progress) {
  is PortalScrapeProgress.Started -> copy(status = PortalScrapeStepStatus.RUNNING)
  is PortalScrapeProgress.Finished -> copy(
    status = PortalScrapeStepStatus.DONE,
    elapsedMillis = progress.elapsedMillis,
    detail = progress.detail,
  )
  is PortalScrapeProgress.Skipped -> copy(status = PortalScrapeStepStatus.SKIPPED, detail = progress.reason)
  is PortalScrapeProgress.Failed -> copy(status = PortalScrapeStepStatus.FAILED, elapsedMillis = progress.elapsedMillis)
}

private fun Throwable.toErrorState(): PortalScrapeErrorState = when (this) {
  is PortalScrapeException -> PortalScrapeErrorState(code = errorCode.name, message = message, retryable = retryable)
  else -> PortalScrapeErrorState(code = this::class.simpleName ?: "UNKNOWN", message = message.orEmpty(), retryable = false)
}
