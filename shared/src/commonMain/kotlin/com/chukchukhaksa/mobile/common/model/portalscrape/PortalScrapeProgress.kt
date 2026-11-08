package com.chukchukhaksa.mobile.common.model.portalscrape

/** 스크래핑 진행 단계. 학생 정보·수강 내역·성적은 스크래퍼와 같이 동시에 조회한다. */
enum class PortalScrapeStep {
  PORTAL_LOGIN,
  ACADEMIC_SSO,
  STUDENT,
  COURSES,
  CREDITS,
  DESIGNATED_COURSES,
  MERGE,

  /** 참고 석차 원본 수집. 성적·수강 응답을 재사용하고, 목록에 없는 이전 정규학기 상세와 학적 조회만 추가로 호출한다. */
  REFERENCE_RANKING,
}

sealed interface PortalScrapeProgress {
  val step: PortalScrapeStep

  data class Started(override val step: PortalScrapeStep) : PortalScrapeProgress

  data class Finished(
    override val step: PortalScrapeStep,
    val elapsedMillis: Long,
    val detail: String? = null,
  ) : PortalScrapeProgress

  data class Skipped(override val step: PortalScrapeStep, val reason: String) : PortalScrapeProgress

  data class Failed(override val step: PortalScrapeStep, val elapsedMillis: Long) : PortalScrapeProgress
}
