package com.chukchukhaksa.mobile.remote.portalscrape

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeErrorCode
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeException
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeStep
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** suwon-scraper-v1 `scrapeJob`의 결과(S3 원문)와 같은 구조. */
internal class PortalScrapeRawResult(
  val student: JsonObject,
  val semesters: JsonArray,
  val academicRecords: JsonObject,
  val designatedCourses: JsonArray,
  val warnings: List<String> = emptyList(),
) {
  fun toJsonObject(): JsonObject = buildJsonObject {
    put("student", student)
    put("semesters", semesters)
    put("academicRecords", academicRecords)
    put("designatedCourses", designatedCourses)
  }
}

/**
 * suwon-scraper-v1의 스크래핑을 기기에서 수행한다.
 * 서버는 Playwright로 포털 화면을 조작했지만, 포털 로그인과 학사 SSO가 모두 일반 폼 전송과 302 리다이렉트로
 * 이뤄지므로 여기서는 브라우저 없이 같은 요청을 보낸다.
 */
internal class SuwonPortalScraper(
  private val clientFactory: () -> HttpClient = ::createPortalHttpClient,
  // 워커의 WORKER_TOTAL_TIMEOUT_MS(120초)와 같다.
  private val totalTimeout: Duration = 120.seconds,
) {
  suspend fun scrape(
    studentId: String,
    password: String,
    onProgress: (PortalScrapeProgress) -> Unit = {},
  ): PortalScrapeRawResult {
    if (studentId.isBlank() || password.isBlank()) {
      throw PortalScrapeException(PortalScrapeErrorCode.INVALID_PAYLOAD, "학번/비밀번호가 필요합니다.", retryable = false)
    }
    val tracker = PortalScrapeProgressTracker(onProgress)
    val client = clientFactory()
    try {
      return withTimeout(totalTimeout) {
        tracker.track(PortalScrapeStep.PORTAL_LOGIN) { client.loginToPortal(studentId, password) }
        tracker.track(PortalScrapeStep.ACADEMIC_SSO) { client.handOffToAcademicSystem() }
        scrapeAuthenticatedData(HttpPortalCrawlers(client), studentId.trim(), tracker)
      }
    } catch (e: TimeoutCancellationException) {
      throw PortalScrapeException(
        errorCode = PortalScrapeErrorCode.PORTAL_TIMEOUT,
        message = "포털 조회가 ${totalTimeout.inWholeSeconds}초 안에 끝나지 않았습니다.",
        retryable = true,
        cause = e,
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      throw classifyPortalError(e)
    } finally {
      client.close()
    }
  }
}

/**
 * 로그인된 학사 세션으로 학생·수강·성적을 동시에 조회하고, 편입생이면 지정과목을 추가로 조회한다.
 * 스크래퍼의 `scrapeAuthenticatedData`와 같이 수강·성적은 학생 정보 완료를 기다리지 않는다.
 */
internal suspend fun scrapeAuthenticatedData(
  crawlers: PortalCrawlers,
  studentId: String,
  tracker: PortalScrapeProgressTracker = PortalScrapeProgressTracker(),
): PortalScrapeRawResult = coroutineScope {
  val student = async { tracker.track(PortalScrapeStep.STUDENT) { crawlers.scrapeStudent(studentId) } }
  val courses = async {
    tracker.track(PortalScrapeStep.COURSES, detail = { "${it.size}과목" }) { crawlers.scrapeCourses(studentId) }
  }
  val credits = async {
    tracker.track(PortalScrapeStep.CREDITS, detail = { "${it.creditDTOs.size}과목" }) { crawlers.scrapeCredits(studentId) }
  }
  val designatedCourses = async {
    if (student.await().isTransferStudent()) {
      tracker.track(PortalScrapeStep.DESIGNATED_COURSES, detail = { "${it.size}건" }) {
        crawlers.scrapeDesignatedCourses(studentId)
      }
    } else {
      tracker.skip(PortalScrapeStep.DESIGNATED_COURSES, reason = "편입생이 아님")
      JsonArray(emptyList())
    }
  }

  val courseDTOs = courses.await()
  val creditResult = credits.await()
  val semesters = tracker.track(PortalScrapeStep.MERGE, detail = { "${it.size}개 학기" }) {
    mergeCreditCourse(creditResult.creditDTOs, courseDTOs)
  }
  PortalScrapeRawResult(
    student = student.await(),
    semesters = semesters,
    academicRecords = creditResult.gradeResponse,
    designatedCourses = designatedCourses.await(),
    warnings = creditResult.warnings,
  )
}

/** 입학 구분 코드가 문자열 "2"이면 편입생이다(스크래퍼의 `student.enscDvcd === "2"`). */
internal fun JsonObject.isTransferStudent(): Boolean =
  (this["enscDvcd"] as? JsonPrimitive)?.let { it.isString && it.content == "2" } == true

/** suwon-scraper-v1 `errorClassifier.ts`와 같은 기준으로 예외를 분류한다. */
internal fun classifyPortalError(error: Throwable): PortalScrapeException {
  if (error is PortalScrapeException) return error
  val message = error.message ?: error::class.simpleName ?: "unknown error"
  val normalizedMessage = message.uppercase()
  return when {
    error is HttpRequestTimeoutException || error is ConnectTimeoutException || error is SocketTimeoutException ||
      listOf("ABORT", "TIMEOUT", "ETIMEDOUT").any { it in normalizedMessage } ->
      PortalScrapeException(PortalScrapeErrorCode.PORTAL_TIMEOUT, message, retryable = true, cause = error)

    error is IOException || listOf("EAI_AGAIN", "ECONNRESET", "ECONNREFUSED").any { it in normalizedMessage } ->
      PortalScrapeException(PortalScrapeErrorCode.PORTAL_TEMPORARY_UNAVAILABLE, message, retryable = true, cause = error)

    else -> PortalScrapeException(PortalScrapeErrorCode.UNKNOWN_NON_RETRYABLE, message, retryable = false, cause = error)
  }
}

internal class PortalScrapeProgressTracker(
  private val onProgress: (PortalScrapeProgress) -> Unit = {},
) {
  suspend fun <T> track(step: PortalScrapeStep, detail: (T) -> String? = { null }, block: suspend () -> T): T {
    onProgress(PortalScrapeProgress.Started(step))
    val mark = TimeSource.Monotonic.markNow()
    val result = try {
      block()
    } catch (e: CancellationException) {
      // 다른 단계의 실패로 함께 취소된 단계는 실패로 표시하지 않는다.
      throw e
    } catch (e: Exception) {
      onProgress(PortalScrapeProgress.Failed(step, mark.elapsedNow().inWholeMilliseconds))
      throw e
    }
    onProgress(PortalScrapeProgress.Finished(step, mark.elapsedNow().inWholeMilliseconds, detail(result)))
    return result
  }

  fun skip(step: PortalScrapeStep, reason: String) {
    onProgress(PortalScrapeProgress.Skipped(step, reason))
  }
}
