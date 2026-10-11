package com.chukchukhaksa.mobile.presentation.portalscrape

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeResult
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeStep
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

data class PortalScrapeState(
  val studentId: String = "",
  val password: String = "",
  val isPasswordVisible: Boolean = false,
  val isRunning: Boolean = false,
  val steps: PersistentList<PortalScrapeStepState> = initialStepStates(),
  val result: PortalScrapeResult? = null,
  val error: PortalScrapeErrorState? = null,
) {
  val canStart: Boolean
    get() = studentId.isNotBlank() && password.isNotBlank() && !isRunning
}

data class PortalScrapeStepState(
  val step: PortalScrapeStep,
  val status: PortalScrapeStepStatus = PortalScrapeStepStatus.PENDING,
  val elapsedMillis: Long? = null,
  val detail: String? = null,
)

enum class PortalScrapeStepStatus {
  PENDING,
  RUNNING,
  DONE,
  SKIPPED,
  FAILED,

  /** 다른 단계가 실패해 함께 취소된 단계. */
  INTERRUPTED,
}

data class PortalScrapeErrorState(
  val code: String,
  val message: String,
  val retryable: Boolean,
)

sealed interface PortalScrapeSideEffect {
  data object PopBackStack : PortalScrapeSideEffect
  data class CopyRawJson(val json: String) : PortalScrapeSideEffect
}

internal fun initialStepStates(): PersistentList<PortalScrapeStepState> =
  PortalScrapeStep.entries.map { PortalScrapeStepState(it) }.toPersistentList()
