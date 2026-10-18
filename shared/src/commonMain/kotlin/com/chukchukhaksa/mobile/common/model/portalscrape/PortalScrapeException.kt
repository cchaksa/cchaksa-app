package com.chukchukhaksa.mobile.common.model.portalscrape

/**
 * suwon-scraper-v1 워커의 `WorkerErrorCode` 중 기기 안에서 발생할 수 있는 코드.
 * 콜백 전송·S3 업로드 관련 코드는 서버 전용이라 옮기지 않았다.
 */
enum class PortalScrapeErrorCode {
  INVALID_PAYLOAD,
  PORTAL_TIMEOUT,
  PORTAL_TEMPORARY_UNAVAILABLE,
  PORTAL_RESPONSE_SCHEMA_MISMATCH,
  PORTAL_AUTH_FAILED,
  PORTAL_ACCOUNT_LOCKED,
  BUSINESS_RULE_VIOLATION,
  UNKNOWN_NON_RETRYABLE,
}

/** 스크래퍼의 `ScrapeJobError`와 같은 의미의 예외. */
class PortalScrapeException(
  val errorCode: PortalScrapeErrorCode,
  override val message: String,
  val retryable: Boolean,
  cause: Throwable? = null,
) : Exception(message, cause)
