package com.chukchukhaksa.mobile.common.model.portalscrape

data class PortalScrapeResult(
  val student: PortalStudent,
  val semesters: List<PortalSemester>,
  val cumulativeRecord: PortalCumulativeRecord?,
  val designatedCourseCount: Int,
  /** 스크래퍼처럼 건너뛰고 진행한 학기별 성적 상세 조회 실패 내역. */
  val warnings: List<String>,
  val elapsedMillis: Long,
  /** 스크래퍼가 S3에 저장하던 원문과 같은 구조(`student`, `semesters`, `academicRecords`, `designatedCourses`)의 JSON. */
  val rawJson: String,
)

data class PortalStudent(
  val studentNumber: String?,
  val name: String?,
  val college: String?,
  val department: String?,
  val major: String?,
  val status: String?,
  val grade: Int?,
  val completedSemesterCount: Int,
  val admissionYear: String?,
  val admissionSemesterCode: String?,
  val isTransfer: Boolean,
  val foreignLanguageStatus: String?,
)

data class PortalSemester(
  val year: String,
  val semesterCode: String,
  val courseCount: Int,
  val totalPoint: Double,
)

/** 포털 누적 성적(`selectSmrCretSumTabSjTotal`). 포털이 문자열로 내려주는 값을 그대로 둔다. */
data class PortalCumulativeRecord(
  val gpa: String?,
  val earnedCredits: String?,
  val attemptedCredits: String?,
  val percentile: String?,
)
