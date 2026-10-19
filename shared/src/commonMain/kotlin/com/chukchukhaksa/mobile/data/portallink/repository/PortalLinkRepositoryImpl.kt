package com.chukchukhaksa.mobile.data.portallink.repository

import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkException
import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkFailure
import com.chukchukhaksa.mobile.common.model.portallink.PortalScrapeUpload
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeErrorCode
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeException
import com.chukchukhaksa.mobile.data.portallink.datasource.RemotePortalLinkDataSource
import com.chukchukhaksa.mobile.data.portallink.debug.PortalLinkDebugSettings
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJob
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJobStatus
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkStrategy
import com.chukchukhaksa.mobile.data.portalscrape.datasource.RemotePortalScrapeDataSource
import com.chukchukhaksa.mobile.remote.common.ApiException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class PortalLinkRepositoryImpl(
  private val remotePortalScrapeDataSource: RemotePortalScrapeDataSource,
  private val remotePortalLinkDataSource: RemotePortalLinkDataSource,
  private val debugSettings: PortalLinkDebugSettings = PortalLinkDebugSettings(),
  private val newIdempotencyKey: () -> String = { Uuid.random().toString() },
) : PortalLinkRepository {

  override suspend fun link(studentId: String, password: String) {
    val strategy = debugSettings.strategy.value
    if (strategy != PortalLinkStrategy.DEVICE_FIRST) {
      Napier.i(tag = LOG_TAG) { "디버그 설정의 연동 경로: $strategy" }
    }
    if (strategy == PortalLinkStrategy.SERVER_ONLY) {
      linkOnServer(studentId, password)
      return
    }
    val fallsBackToServer = strategy == PortalLinkStrategy.DEVICE_FIRST

    val scrapeResult = try {
      remotePortalScrapeDataSource.scrape(studentId, password, onProgress = {})
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val errorCode = (e as? PortalScrapeException)?.errorCode
      if (!fallsBackToServer || (errorCode != null && errorCode in DEVICE_ONLY_ERROR_CODES)) {
        val failure = errorCode?.let(::deviceFailureOf) ?: PortalLinkFailure.System(PortalLinkFailure.UNKNOWN_CODE)
        throw PortalLinkException(failure, e)
      }
      Napier.w(tag = LOG_TAG, throwable = e) { "기기 스크래핑 실패(${errorCode ?: e::class.simpleName}) → 서버 job으로 전환" }
      linkOnServer(studentId, password)
      return
    }
    // 서버는 올린 결과를 전체 기록으로 보고 저장한다(빠진 과목은 지운다). 그래서 일부 학기 조회가 실패한 결과는 올리지 않는다.
    if (scrapeResult.warnings.isNotEmpty()) {
      if (!fallsBackToServer) {
        throw PortalLinkException(PortalLinkFailure.System(PARTIAL_SCRAPE_CODE))
      }
      Napier.w(tag = LOG_TAG) { "기기 스크래핑 일부 실패(${scrapeResult.warnings.joinToString()}) → 서버 job으로 전환" }
      linkOnServer(studentId, password)
      return
    }
    upload(PortalScrapeUpload(scrapeResult, idempotencyKey = newIdempotencyKey()))
  }

  override suspend fun upload(upload: PortalScrapeUpload) {
    for (attempt in 1..UPLOAD_MAX_ATTEMPTS) {
      try {
        remotePortalLinkDataSource.uploadScrapeResult(upload.scrapeResult.rawJson, upload.idempotencyKey)
        return
      } catch (e: CancellationException) {
        throw e
      } catch (e: ApiException) {
        if (e.code == SNAPSHOT_STALE_CODE) {
          // 이 결과보다 나중에 접수된 결과가 이미 반영됐다. 이 결과는 버리고 연동이 끝난 것으로 본다.
          Napier.i(tag = LOG_TAG) { "더 최신 포털 데이터가 이미 반영됨(${e.code}) → 업로드 중단" }
          return
        }
        // 서버가 내용을 보고 거부했으므로 같은 결과를 다시 올려도 결과가 같다. 다시 시도하려면 새로 스크래핑한다.
        throw PortalLinkException(serverFailureOf(e.code.ifEmpty { PortalLinkFailure.UNKNOWN_CODE }), e)
      } catch (e: Exception) {
        // 5xx·네트워크 오류·응답 유실은 서버에 저장됐는지 알 수 없다. 같은 키로 같은 결과를 보내면 두 번 반영되지 않는다.
        if (attempt == UPLOAD_MAX_ATTEMPTS) {
          throw PortalLinkException(PortalLinkFailure.UploadFailed(upload), e)
        }
        Napier.w(tag = LOG_TAG, throwable = e) { "스크래핑 결과 업로드 실패($attempt/$UPLOAD_MAX_ATTEMPTS) → 다시 시도" }
        delay(UPLOAD_RETRY_BASE_DELAY * attempt)
      }
    }
  }

  private suspend fun linkOnServer(studentId: String, password: String) {
    val jobId = try {
      remotePortalLinkDataSource.createLinkJob(studentId, password, newIdempotencyKey())
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val code = (e as? ApiException)?.code?.ifEmpty { null } ?: PortalLinkFailure.UNKNOWN_CODE
      throw PortalLinkException(serverFailureOf(code), e)
    }

    val job = withTimeoutOrNull(SERVER_JOB_TIMEOUT) { pollUntilFinished(jobId) }
    when {
      job == null -> {
        // 마지막 폴링을 놓쳤을 수 있으니 요약으로 실제 성공 여부를 한 번 더 확인한다(웹과 같은 처리).
        if (summaryStatusOrNull(jobId) != PortalLinkJobStatus.SUCCEEDED) {
          throw PortalLinkException(PortalLinkFailure.Timeout)
        }
      }

      job.status == PortalLinkJobStatus.FAILED -> {
        throw PortalLinkException(serverFailureOf(job.errorCode ?: PortalLinkFailure.UNKNOWN_CODE))
      }
    }
  }

  private suspend fun pollUntilFinished(jobId: String): PortalLinkJob {
    while (true) {
      val job = try {
        remotePortalLinkDataSource.getLinkJob(jobId)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        // 한 번의 조회 실패로 연동을 실패 처리하지 않는다. 제한 시간까지 계속 조회한다.
        Napier.w(tag = LOG_TAG, throwable = e) { "서버 job 상태 조회 실패" }
        null
      }
      if (job != null && job.status != PortalLinkJobStatus.IN_PROGRESS) return job
      delay(POLLING_INTERVAL)
    }
  }

  private suspend fun summaryStatusOrNull(jobId: String): PortalLinkJobStatus? = try {
    remotePortalLinkDataSource.getLinkJobSummaryStatus(jobId)
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    Napier.w(tag = LOG_TAG, throwable = e) { "서버 job 요약 조회 실패" }
    null
  }

  private fun deviceFailureOf(errorCode: PortalScrapeErrorCode): PortalLinkFailure = when (errorCode) {
    PortalScrapeErrorCode.PORTAL_AUTH_FAILED,
    PortalScrapeErrorCode.PORTAL_ACCOUNT_LOCKED,
    -> PortalLinkFailure.Credential(errorCode.name)

    else -> PortalLinkFailure.System(errorCode.name)
  }

  private fun serverFailureOf(code: String): PortalLinkFailure = when (code) {
    in CREDENTIAL_CODES -> PortalLinkFailure.Credential(code)
    in INELIGIBLE_CODES -> PortalLinkFailure.Ineligible(code)
    else -> PortalLinkFailure.System(code)
  }

  private companion object {
    const val LOG_TAG = "PortalLinkRepository"

    /** 서버에서 다시 스크래핑해도 결과가 같은 실패. 이 코드들은 서버 job으로 넘기지 않는다. */
    val DEVICE_ONLY_ERROR_CODES = setOf(
      PortalScrapeErrorCode.PORTAL_AUTH_FAILED,
      PortalScrapeErrorCode.PORTAL_ACCOUNT_LOCKED,
      PortalScrapeErrorCode.INVALID_PAYLOAD,
    )

    /** 웹 errorMapping.ts의 분류와 같다. 서버가 자격 증명 실패 코드를 두 가지 이름으로 내려준다. */
    val CREDENTIAL_CODES = setOf("PORTAL_AUTH_FAILED", "PORTAL_LOGIN_FAILED", "PORTAL_ACCOUNT_LOCKED")

    /** S04·D01·T13·G02는 웹과 같다. C21은 업로드한 결과의 학번이 다른 계정에 연결됐거나 연결된 학번과 다를 때 온다. */
    val INELIGIBLE_CODES = setOf("S04", "D01", "T13", "G02", "C21")

    /** 업로드한 결과보다 최신 포털 데이터가 이미 반영됐다. */
    const val SNAPSHOT_STALE_CODE = "C22"

    /** 서버 job으로 넘어가지 않는 경로에서 일부 학기 조회가 실패한 결과를 버릴 때 남기는 코드. */
    const val PARTIAL_SCRAPE_CODE = "PARTIAL_SCRAPE"

    const val UPLOAD_MAX_ATTEMPTS = 3
    val UPLOAD_RETRY_BASE_DELAY = 1.seconds

    // 웹 usePortalLinkJobPolling과 같은 값이다.
    val POLLING_INTERVAL = 2.seconds
    val SERVER_JOB_TIMEOUT = 3.minutes
  }
}
