package com.chukchukhaksa.mobile.data.portallink.repository

import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkException
import com.chukchukhaksa.mobile.common.model.portallink.PortalLinkFailure
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeErrorCode
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeException
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeResult
import com.chukchukhaksa.mobile.common.model.portalscrape.portalScrapeResultFixture
import com.chukchukhaksa.mobile.data.portallink.datasource.RemotePortalLinkDataSource
import com.chukchukhaksa.mobile.data.portallink.debug.PortalLinkDebugSettings
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJob
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJobStatus
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkStrategy
import com.chukchukhaksa.mobile.data.portalscrape.datasource.RemotePortalScrapeDataSource
import com.chukchukhaksa.mobile.remote.common.ApiException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class PortalLinkRepositoryImplTest {

  private val scrapeResult = portalScrapeResultFixture()

  private var keyCount = 0
  private val scrapeDataSource = FakeScrapeDataSource()
  private val linkDataSource = FakeLinkDataSource()
  private val debugSettings = PortalLinkDebugSettings()
  private val repository = PortalLinkRepositoryImpl(
    remotePortalScrapeDataSource = scrapeDataSource,
    remotePortalLinkDataSource = linkDataSource,
    debugSettings = debugSettings,
    newIdempotencyKey = { "key-${++keyCount}" },
  )

  @Test
  fun `device scrape success uploads raw json without creating a server job`() = runTest {
    scrapeDataSource.result = { scrapeResult }

    repository.link("20231234", "pw")

    assertEquals(listOf(Upload(scrapeResult.rawJson, "key-1")), linkDataSource.uploads)
    assertTrue(linkDataSource.createdJobs.isEmpty())
  }

  @Test
  fun `scrape result with warnings is not uploaded and falls back to a server job`() = runTest {
    scrapeDataSource.result = { portalScrapeResultFixture(warnings = listOf("2024-10 학기 성적 상세 조회에 실패해 건너뛰었습니다.")) }
    linkDataSource.jobs = ArrayDeque(listOf(job(PortalLinkJobStatus.SUCCEEDED)))

    repository.link("20231234", "pw")

    assertTrue(linkDataSource.uploads.isEmpty())
    assertEquals(listOf(CreatedJob("20231234", "pw", "key-1")), linkDataSource.createdJobs)
  }

  @Test
  fun `credential failures on device do not fall back to the server`() = runTest {
    for (errorCode in listOf(PortalScrapeErrorCode.PORTAL_AUTH_FAILED, PortalScrapeErrorCode.PORTAL_ACCOUNT_LOCKED)) {
      scrapeDataSource.result = { throw PortalScrapeException(errorCode, "실패", retryable = false) }

      val failure = failureOf { repository.link("20231234", "pw") }

      assertEquals(PortalLinkFailure.Credential(errorCode.name), failure)
    }
    assertTrue(linkDataSource.createdJobs.isEmpty())
  }

  @Test
  fun `device side failures fall back to a server job with the same credentials`() = runTest {
    val deviceErrors = listOf(
      PortalScrapeException(PortalScrapeErrorCode.PORTAL_RESPONSE_SCHEMA_MISMATCH, "구조 변경", retryable = false),
      PortalScrapeException(PortalScrapeErrorCode.BUSINESS_RULE_VIOLATION, "SSO 실패", retryable = false),
      PortalScrapeException(PortalScrapeErrorCode.PORTAL_TIMEOUT, "시간 초과", retryable = true),
      IllegalStateException("예상하지 못한 오류"),
    )
    for (error in deviceErrors) {
      scrapeDataSource.result = { throw error }
      linkDataSource.jobs = ArrayDeque(listOf(job(PortalLinkJobStatus.IN_PROGRESS), job(PortalLinkJobStatus.SUCCEEDED)))

      repository.link("20231234", "pw")
    }

    assertEquals(deviceErrors.size, linkDataSource.createdJobs.size)
    assertTrue(linkDataSource.createdJobs.all { it.studentId == "20231234" && it.password == "pw" })
    assertTrue(linkDataSource.uploads.isEmpty())
  }

  @Test
  fun `failed server job is classified by its error code`() = runTest {
    scrapeDataSource.result = { throw IOException("network") }
    val expected = mapOf(
      "PORTAL_LOGIN_FAILED" to PortalLinkFailure.Credential("PORTAL_LOGIN_FAILED"),
      "PORTAL_ACCOUNT_LOCKED" to PortalLinkFailure.Credential("PORTAL_ACCOUNT_LOCKED"),
      "S04" to PortalLinkFailure.Ineligible("S04"),
      "X99" to PortalLinkFailure.System("X99"),
      null to PortalLinkFailure.System(PortalLinkFailure.UNKNOWN_CODE),
    )
    for ((errorCode, failure) in expected) {
      linkDataSource.jobs = ArrayDeque(listOf(PortalLinkJob(PortalLinkJobStatus.FAILED, errorCode)))

      assertEquals(failure, failureOf { repository.link("20231234", "pw") })
    }
  }

  @Test
  fun `server job creation errors become failures`() = runTest {
    scrapeDataSource.result = { throw IOException("network") }

    linkDataSource.createError = ApiException(code = "SCRAPE_JOB_DUPLICATED", message = "중복")
    assertEquals(PortalLinkFailure.System("SCRAPE_JOB_DUPLICATED"), failureOf { repository.link("20231234", "pw") })

    linkDataSource.createError = IOException("network")
    assertEquals(PortalLinkFailure.System(PortalLinkFailure.UNKNOWN_CODE), failureOf { repository.link("20231234", "pw") })
  }

  @Test
  fun `polling errors are retried until the job finishes`() = runTest {
    scrapeDataSource.result = { throw IOException("network") }
    linkDataSource.pollErrorsBeforeJobs = 2
    linkDataSource.jobs = ArrayDeque(listOf(job(PortalLinkJobStatus.SUCCEEDED)))

    repository.link("20231234", "pw")

    assertEquals(3, linkDataSource.pollCount)
  }

  @Test
  fun `job still running after three minutes succeeds when the summary says so`() = runTest {
    scrapeDataSource.result = { throw IOException("network") }
    linkDataSource.jobs = ArrayDeque(listOf(job(PortalLinkJobStatus.IN_PROGRESS)))
    linkDataSource.summaryStatus = PortalLinkJobStatus.SUCCEEDED

    repository.link("20231234", "pw")

    assertTrue(currentTime >= 3.minutes.inWholeMilliseconds)
  }

  @Test
  fun `job still running after three minutes times out`() = runTest {
    scrapeDataSource.result = { throw IOException("network") }
    linkDataSource.jobs = ArrayDeque(listOf(job(PortalLinkJobStatus.IN_PROGRESS)))
    linkDataSource.summaryStatus = PortalLinkJobStatus.IN_PROGRESS

    assertEquals(PortalLinkFailure.Timeout, failureOf { repository.link("20231234", "pw") })
  }

  @Test
  fun `transient upload errors are retried with the same key and a new scrape gets a new key`() = runTest {
    scrapeDataSource.result = { scrapeResult }
    linkDataSource.uploadErrors = ArrayDeque(listOf(IOException("1"), IOException("2")))

    repository.link("20231234", "pw")
    assertEquals(List(3) { Upload(scrapeResult.rawJson, "key-1") }, linkDataSource.uploads)

    repository.link("20231234", "pw")
    assertEquals(Upload(scrapeResult.rawJson, "key-2"), linkDataSource.uploads.last())
  }

  @Test
  fun `upload that keeps failing keeps the result and its key for a later retry`() = runTest {
    scrapeDataSource.result = { scrapeResult }
    linkDataSource.uploadErrors = ArrayDeque(List(3) { IOException("$it") })

    val failure = failureOf { repository.link("20231234", "pw") }

    assertIs<PortalLinkFailure.UploadFailed>(failure)
    assertSame(scrapeResult, failure.upload.scrapeResult)
    assertEquals(PortalLinkFailure.UPLOAD_FAILED_CODE, failure.code)
    assertTrue(linkDataSource.createdJobs.isEmpty(), "업로드 실패는 서버 job으로 넘기지 않는다")

    repository.upload(failure.upload)
    assertEquals(List(4) { Upload(scrapeResult.rawJson, "key-1") }, linkDataSource.uploads)
    assertEquals(1, scrapeDataSource.calls, "다시 올릴 때는 스크래핑하지 않는다")
  }

  @Test
  fun `upload rejected by the server is not retried`() = runTest {
    scrapeDataSource.result = { scrapeResult }
    val expected = mapOf(
      "C21" to PortalLinkFailure.Ineligible("C21"),
      "C17" to PortalLinkFailure.System("C17"),
      "" to PortalLinkFailure.System(PortalLinkFailure.UNKNOWN_CODE),
    )
    for ((code, failure) in expected) {
      linkDataSource.uploads.clear()
      linkDataSource.uploadErrors = ArrayDeque(listOf(ApiException(code = code, message = "거부")))

      assertEquals(failure, failureOf { repository.link("20231234", "pw") })
      assertEquals(1, linkDataSource.uploads.size)
    }
    assertTrue(linkDataSource.createdJobs.isEmpty(), "업로드 거부는 서버 job으로 넘기지 않는다")
  }

  @Test
  fun `stale snapshot response ends the link without retrying`() = runTest {
    scrapeDataSource.result = { scrapeResult }
    linkDataSource.uploadErrors = ArrayDeque(listOf(ApiException(code = "C22", message = "더 최신 데이터")))

    repository.link("20231234", "pw")

    assertEquals(1, linkDataSource.uploads.size)
    assertTrue(linkDataSource.createdJobs.isEmpty())
  }

  @Test
  fun `server only strategy creates a server job without scraping`() = runTest {
    debugSettings.setStrategy(PortalLinkStrategy.SERVER_ONLY)
    linkDataSource.jobs = ArrayDeque(listOf(job(PortalLinkJobStatus.SUCCEEDED)))

    repository.link("20231234", "pw")

    assertEquals(0, scrapeDataSource.calls)
    assertEquals(listOf(CreatedJob("20231234", "pw", "key-1")), linkDataSource.createdJobs)
    assertTrue(linkDataSource.uploads.isEmpty())
  }

  @Test
  fun `device only strategy reports device failures instead of falling back`() = runTest {
    debugSettings.setStrategy(PortalLinkStrategy.DEVICE_ONLY)
    val expected = listOf(
      PortalScrapeException(PortalScrapeErrorCode.PORTAL_TEMPORARY_UNAVAILABLE, "네트워크", retryable = true) to
        PortalLinkFailure.System("PORTAL_TEMPORARY_UNAVAILABLE"),
      PortalScrapeException(PortalScrapeErrorCode.PORTAL_AUTH_FAILED, "비밀번호", retryable = false) to
        PortalLinkFailure.Credential("PORTAL_AUTH_FAILED"),
      IllegalStateException("예상하지 못한 오류") to PortalLinkFailure.System(PortalLinkFailure.UNKNOWN_CODE),
    )
    for ((error, failure) in expected) {
      scrapeDataSource.result = { throw error }

      assertEquals(failure, failureOf { repository.link("20231234", "pw") })
    }
    assertTrue(linkDataSource.createdJobs.isEmpty())
  }

  @Test
  fun `device only strategy drops a result with warnings without uploading`() = runTest {
    debugSettings.setStrategy(PortalLinkStrategy.DEVICE_ONLY)
    scrapeDataSource.result = { portalScrapeResultFixture(warnings = listOf("2024-10 학기 성적 상세 조회에 실패해 건너뛰었습니다.")) }

    assertEquals(PortalLinkFailure.System("PARTIAL_SCRAPE"), failureOf { repository.link("20231234", "pw") })
    assertTrue(linkDataSource.uploads.isEmpty())
    assertTrue(linkDataSource.createdJobs.isEmpty())
  }

  @Test
  fun `device only strategy uploads a complete result`() = runTest {
    debugSettings.setStrategy(PortalLinkStrategy.DEVICE_ONLY)
    scrapeDataSource.result = { scrapeResult }

    repository.link("20231234", "pw")

    assertEquals(listOf(Upload(scrapeResult.rawJson, "key-1")), linkDataSource.uploads)
  }

  private suspend fun failureOf(block: suspend () -> Unit): PortalLinkFailure =
    assertFailsWith<PortalLinkException> { block() }.failure

  private fun job(status: PortalLinkJobStatus) = PortalLinkJob(status = status, errorCode = null)

  private class FakeScrapeDataSource : RemotePortalScrapeDataSource {
    var result: () -> PortalScrapeResult = { error("결과를 정하지 않았습니다") }
    var calls = 0

    override suspend fun scrape(
      studentId: String,
      password: String,
      onProgress: (PortalScrapeProgress) -> Unit,
    ): PortalScrapeResult {
      calls++
      return result()
    }
  }

  private data class CreatedJob(val studentId: String, val password: String, val idempotencyKey: String)

  private data class Upload(val rawJson: String, val idempotencyKey: String)

  private class FakeLinkDataSource : RemotePortalLinkDataSource {
    val uploads = mutableListOf<Upload>()
    var uploadErrors = ArrayDeque<Exception>()
    val createdJobs = mutableListOf<CreatedJob>()
    var createError: Exception? = null

    /** 폴링마다 하나씩 꺼낸다. 마지막 하나는 계속 돌려준다. */
    var jobs = ArrayDeque<PortalLinkJob>()
    var pollErrorsBeforeJobs = 0
    var pollCount = 0
    var summaryStatus = PortalLinkJobStatus.IN_PROGRESS

    override suspend fun uploadScrapeResult(rawJson: String, idempotencyKey: String) {
      uploads += Upload(rawJson, idempotencyKey)
      uploadErrors.removeFirstOrNull()?.let { throw it }
    }

    override suspend fun createLinkJob(studentId: String, password: String, idempotencyKey: String): String {
      createError?.let { throw it }
      createdJobs += CreatedJob(studentId, password, idempotencyKey)
      return "job-${createdJobs.size}"
    }

    override suspend fun getLinkJob(jobId: String): PortalLinkJob {
      pollCount++
      if (pollCount <= pollErrorsBeforeJobs) throw IOException("poll $pollCount")
      return if (jobs.size > 1) jobs.removeFirst() else jobs.first()
    }

    override suspend fun getLinkJobSummaryStatus(jobId: String): PortalLinkJobStatus = summaryStatus
  }
}
