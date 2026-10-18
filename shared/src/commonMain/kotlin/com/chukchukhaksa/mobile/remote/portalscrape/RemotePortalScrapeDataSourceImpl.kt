package com.chukchukhaksa.mobile.remote.portalscrape

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalCumulativeRecord
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeProgress
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeResult
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalSemester
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalStudent
import com.chukchukhaksa.mobile.data.portalscrape.datasource.RemotePortalScrapeDataSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlin.time.TimeSource

class RemotePortalScrapeDataSourceImpl internal constructor(
  private val scraper: SuwonPortalScraper,
) : RemotePortalScrapeDataSource {

  constructor() : this(SuwonPortalScraper())

  override suspend fun scrape(
    studentId: String,
    password: String,
    onProgress: (PortalScrapeProgress) -> Unit,
  ): PortalScrapeResult {
    val mark = TimeSource.Monotonic.markNow()
    val rawResult = scraper.scrape(studentId, password, onProgress)
    return rawResult.toPortalScrapeResult(elapsedMillis = mark.elapsedNow().inWholeMilliseconds)
  }
}

private val prettyJson = Json { prettyPrint = true }

internal fun PortalScrapeRawResult.toPortalScrapeResult(elapsedMillis: Long): PortalScrapeResult {
  val total = academicRecords["selectSmrCretSumTabSjTotal"] as? JsonObject
  return PortalScrapeResult(
    student = PortalStudent(
      studentNumber = student.text("sno"),
      name = student.text("studNm"),
      college = student.text("univNm"),
      department = student.text("dpmjNm"),
      major = student.text("mjorNm"),
      status = student.text("scrgStatNm"),
      grade = student.text("studGrde")?.toDoubleOrNull()?.toInt(),
      completedSemesterCount = student.text("facSmrCnt")?.toIntOrNull() ?: 0,
      admissionYear = student.text("enscYear"),
      admissionSemesterCode = student.text("enscSmrCd"),
      isTransfer = student.isTransferStudent(),
      foreignLanguageStatus = student.text("flangPassGb"),
    ),
    semesters = semesters.filterIsInstance<JsonObject>().map { it.toPortalSemester() },
    cumulativeRecord = total?.let {
      PortalCumulativeRecord(
        gpa = it.text("gainAvmk"),
        earnedCredits = it.text("gainPoint"),
        attemptedCredits = it.text("applPoint"),
        percentile = it.text("gainTavgPont"),
      )
    },
    designatedCourseCount = designatedCourses.size,
    warnings = warnings,
    elapsedMillis = elapsedMillis,
    rawJson = prettyJson.encodeToString(JsonObject.serializer(), toJsonObject()),
  )
}

private fun JsonObject.toPortalSemester(): PortalSemester {
  val key = text("semester").orEmpty()
  val courses = (this["courses"] as? JsonArray).orEmpty().filterIsInstance<JsonObject>()
  return PortalSemester(
    year = key.substringBefore('-'),
    semesterCode = key.substringAfter('-', missingDelimiterValue = ""),
    courseCount = courses.size,
    totalPoint = courses.sumOf { (it["point"] as? JsonPrimitive)?.doubleOrNull ?: 0.0 },
  )
}

private fun JsonObject.text(key: String): String? =
  (this[key] as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content
