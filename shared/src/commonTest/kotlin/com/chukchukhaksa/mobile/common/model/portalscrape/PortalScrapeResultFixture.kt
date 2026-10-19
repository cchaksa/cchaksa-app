package com.chukchukhaksa.mobile.common.model.portalscrape

internal fun portalScrapeResultFixture(
  studentNumber: String = "20231234",
  warnings: List<String> = emptyList(),
): PortalScrapeResult = PortalScrapeResult(
  student = PortalStudent(
    studentNumber = studentNumber,
    name = null,
    college = null,
    department = null,
    major = null,
    status = null,
    grade = null,
    completedSemesterCount = 0,
    admissionYear = null,
    admissionSemesterCode = null,
    isTransfer = false,
    foreignLanguageStatus = null,
  ),
  semesters = emptyList(),
  cumulativeRecord = null,
  designatedCourseCount = 0,
  warnings = warnings,
  elapsedMillis = 0,
  rawJson = """{"student":{"sno":"$studentNumber"}}""",
)
