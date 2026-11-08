package com.chukchukhaksa.mobile.remote.portalscrape

import io.github.aakira.napier.Napier
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 참고 석차 원본(`payload.referenceRankingRaw`)의 조회 종류. 이름이 서버 계약의 `source` 값이다.
 * 계약: cchaksa-backend `docs/tasks/376/mobile-integration-guide.md`(PR #378).
 *
 * @property targetField 서버가 읽는 응답 필드
 * @property hasTerm 바깥 `year`, `semester`에 조회 학기를 넣는 조회
 * @property allowedFields 개인정보를 빼고 남길 필드. null이면 응답 텍스트를 그대로 보낸다.
 */
internal enum class ReferenceRankingSource(
  val targetField: String,
  val hasTerm: Boolean,
  val allowedFields: Set<String>? = null,
) {
  GRADE_SUMMARY("listSmrCretSumTabYearSmr", hasTerm = false),
  GRADE_DETAIL("listSmrCretSumTabSubjt", hasTerm = true),
  COURSE_TERMS("listAtlecApplDtaiTabYearSmr", hasTerm = false),
  COURSE_DETAIL("listAtlecApplDtaiTabSubjt", hasTerm = true),
  REGISTRATION("listTutnTab", hasTerm = false, allowedFields = setOf("regYearSmr", "studGrde")),
  MAJOR_HISTORY(
    "listMjorChngInfoTab",
    hasTerm = false,
    allowedFields = setOf("mjorChngSeq", "mjorChngYearSmr", "mjorChngNm", "achgMjorNm", "mjorChngCancDt"),
  ),
  MAJOR_ASSIGNMENT(
    "mjorChngAppl",
    hasTerm = true,
    allowedFields = setOf("sno", "mjorChngCd", "applYear", "applSmrCd", "alotMjorCd", "applStatCd", "mjorStatCd"),
  ),
}

/** 정규학기. [semester]는 1학기 10, 2학기 20이다. */
internal data class RegularTerm(val year: Int, val semester: Int) : Comparable<RegularTerm> {
  /** 바로 앞 정규학기. 계절학기를 건너뛴다(2026-1 → 2025-2, 2026-2 → 2026-1). */
  fun previous(): RegularTerm = if (semester == 10) RegularTerm(year - 1, 20) else RegularTerm(year, 10)

  override fun compareTo(other: RegularTerm): Int = compareValuesBy(this, other, { it.year }, { it.semester })

  companion object {
    private val YEAR = Regex("[0-9]{1,4}")
    private val LABEL = Regex("((?:19|20)[0-9]{2})-([12])학기")

    /** 서버 정규화와 같이 숫자 문자열이나 정수인 학년도·학기 코드만 읽는다. 계절학기(15·25)는 null이다. */
    fun of(row: JsonObject, yearKey: String, semesterKey: String): RegularTerm? {
      val year = row.plainText(yearKey)?.takeIf { YEAR.matches(it) }?.toInt() ?: return null
      val semester = row.plainText(semesterKey)?.takeIf { YEAR.matches(it) }?.toInt() ?: return null
      return if (year in 1900..2100 && (semester == 10 || semester == 20)) RegularTerm(year, semester) else null
    }

    /** 학적 응답의 `2025-1학기` 같은 표기. */
    fun ofLabel(label: String?): RegularTerm? {
      val match = label?.trim()?.let(LABEL::matchEntire) ?: return null
      return RegularTerm(match.groupValues[1].toInt(), match.groupValues[2].toInt() * 10)
    }
  }
}

internal const val REGISTRATION_PATH = "/tutnBas/listTutnTabStud.do"
internal const val MAJOR_HISTORY_PATH = "/mjorChngInfo/listMjorChngInfoTab.do"
internal const val MAJOR_ASSIGNMENT_PATH = "/mjorChngAppl/selectMjorChngAppl.do"

// 주전공배정 신청 구분 코드. 근거: 전공배정신청 화면 SA_HJ_3021.xml(백엔드 source-research.md).
private const val MAJOR_ASSIGNMENT_CODE = "11"
private const val MAJOR_ASSIGNMENT_NAME = "주전공배정"

// 서버의 전송 제한(AppReferenceRankingRawValidator). 넘으면 업로드 전체가 거절되므로 원본을 보내지 않는다.
private const val MAX_RESPONSES = 245
private const val MAX_ROWS = 100
private const val MAX_BODY_LENGTH = 200_000
private const val MAX_TOTAL_BODY_BYTES = 2_000_000

/** 세션이 끊긴 학사 API가 HTTP 200과 함께 주는 오류 코드. */
private const val SESSION_EXPIRED_API_CODE = "-998"

/**
 * 학사 자료 조회가 끝난 뒤 참고 석차 원본을 모은다. 이미 받은 성적·수강 응답은 [log]에서 재사용하고,
 * 목록에 없는 이전 정규학기의 상세와 학적 조회 3종만 새로 호출한다. 모바일은 평점이나 순위를 계산하지 않는다.
 *
 * 응답 하나가 서버 계약에 맞지 않으면 그 응답만 `FAILED`로 보내서, 참고 석차 원본 때문에 기본 학사 업로드가 거절되지 않게 한다.
 */
internal class ReferenceRankingRawCollector(
  private val api: PortalAcademicApi,
  private val log: PortalAcademicExchangeLog,
) {
  /**
   * @param studentId 학사 API 요청 본문에 넣을 학번
   * @param studentNumber 업로드의 `payload.student.sno`. 응답 원문에 있는 학번이 이 값과 같아야 한다.
   * @return `referenceRankingRaw` 객체. 서버 전송 제한을 넘으면 null이다(서버는 기존 석차 자료를 유지한다).
   */
  suspend fun collect(studentId: String, studentNumber: String): JsonObject? = coroutineScope {
    val recorded = log.snapshot()
    val summary = recorded.lastOrNull { it.path == GRADE_SUMMARY_PATH } ?: api.exchange(GRADE_SUMMARY_PATH, snoBody(studentId))
    val courseTerms = recorded.lastOrNull { it.path == COURSE_SEMESTERS_PATH }
      ?: api.exchange(COURSE_SEMESTERS_PATH, snoBody(studentId))

    val listedTerms = summary.listedTerms(ReferenceRankingSource.GRADE_SUMMARY, "cretGainYear", "cretSmrCd") +
      courseTerms.listedTerms(ReferenceRankingSource.COURSE_TERMS, "subjtEstbYear", "subjtEstbSmrCd")
    val terms = (listedTerms + listedTerms.map { it.previous() }).filter { it.year >= 1900 }.distinct().sorted()
    // 요약·목록 2건, 학기마다 상세 2건, 학적 2건은 전공배정 전에 정해진다. 이미 제한을 넘으면 조회하지 않는다.
    val fixedResponseCount = 2 + terms.size * 2 + 2
    if (fixedResponseCount > MAX_RESPONSES) {
      Napier.w(tag = LOG_TAG) { "참고 석차 원본이 전송 제한을 넘어 보내지 않음(${fixedResponseCount}건 이상)" }
      return@coroutineScope null
    }

    val registration = async { api.exchange(REGISTRATION_PATH, snoBody(studentId)) }
    val history = async { api.exchange(MAJOR_HISTORY_PATH, snoBody(studentId)) }

    val responses = mutableListOf(
      entry(ReferenceRankingSource.GRADE_SUMMARY, null, summary, studentNumber),
      entry(ReferenceRankingSource.COURSE_TERMS, null, courseTerms, studentNumber),
    )
    // 학사 자료 조회처럼 학기 상세는 하나씩 조회한다.
    for (term in terms) {
      val grade = recorded.detail(CREDIT_DETAIL_PATH, "cretGainYear", "cretSmrCd", term)
        ?: api.exchange(CREDIT_DETAIL_PATH, termBody(studentId, "cretGainYear", "cretSmrCd", term))
      val course = recorded.detail(COURSE_DETAIL_PATH, "subjtEstbYear", "subjtEstbSmrCd", term)
        ?: api.exchange(COURSE_DETAIL_PATH, termBody(studentId, "subjtEstbYear", "subjtEstbSmrCd", term))
      responses += entry(ReferenceRankingSource.GRADE_DETAIL, term, grade, studentNumber)
      responses += entry(ReferenceRankingSource.COURSE_DETAIL, term, course, studentNumber)
    }

    responses += entry(ReferenceRankingSource.REGISTRATION, null, registration.await(), studentNumber)
    val historyExchange = history.await()
    val historyEntry = entry(ReferenceRankingSource.MAJOR_HISTORY, null, historyExchange, studentNumber)
    responses += historyEntry
    // 전공배정 결과는 성적 학기가 아니라 전공 이력의 주전공배정 행에 적힌 학기로 조회한다.
    for (term in historyExchange.majorAssignmentTerms(historyEntry)) {
      val body = buildJsonObject {
        put("sno", studentId)
        put("mjorChngCd", MAJOR_ASSIGNMENT_CODE)
        put("applYear", term.year.toString())
        put("applSmrCd", term.semester.toString())
      }
      responses += entry(ReferenceRankingSource.MAJOR_ASSIGNMENT, term, api.exchange(MAJOR_ASSIGNMENT_PATH, body), studentNumber)
    }

    val totalBytes = responses.sumOf { (it["responseBody"] as? JsonPrimitive)?.takeIf { p -> p.isString }?.content?.encodeToByteArray()?.size ?: 0 }
    if (responses.size > MAX_RESPONSES || totalBytes > MAX_TOTAL_BODY_BYTES) {
      Napier.w(tag = LOG_TAG) { "참고 석차 원본이 전송 제한을 넘어 보내지 않음(${responses.size}건, ${totalBytes}B)" }
      return@coroutineScope null
    }
    buildJsonObject {
      put("schemaVersion", 1)
      put("responses", JsonArray(responses))
    }
  }

  private fun List<PortalAcademicExchange>.detail(
    path: String,
    yearKey: String,
    semesterKey: String,
    term: RegularTerm,
  ): PortalAcademicExchange? = lastOrNull { it.path == path && RegularTerm.of(it.request, yearKey, semesterKey) == term }

  private fun PortalAcademicExchange.listedTerms(source: ReferenceRankingSource, yearKey: String, semesterKey: String): Set<RegularTerm> =
    (receivedObjectOrNull()?.get(source.targetField) as? JsonArray).orEmpty()
      .filterIsInstance<JsonObject>()
      .mapNotNullTo(mutableSetOf()) { RegularTerm.of(it, yearKey, semesterKey) }

  private fun PortalAcademicExchange.majorAssignmentTerms(sent: JsonObject): List<RegularTerm> {
    // 서버로 보낼 수 있는 이력에서만 조회 학기를 고른다. 보내지 못하는 이력으로 조회한 결과는 서버가 쓰지 않는다.
    if (sent["errorCode"] != JsonNull) return emptyList()
    return (receivedObjectOrNull()?.get(ReferenceRankingSource.MAJOR_HISTORY.targetField) as? JsonArray).orEmpty()
      .filterIsInstance<JsonObject>()
      .filter { it.plainText("mjorChngNm") == MAJOR_ASSIGNMENT_NAME && it.plainText("mjorChngCancDt").isNullOrEmpty() }
      .mapNotNull { RegularTerm.ofLabel(it.plainText("mjorChngYearSmr")) }
      .distinct()
      .sorted()
  }
}

/**
 * 조회 한 번을 서버 계약의 `responses[]` 항목으로 바꾼다. 서버 검증(AppReferenceRankingRawValidator)과 같은 조건을
 * 먼저 확인해서, 맞지 않는 응답은 `FAILED`로 보낸다.
 */
internal fun entry(
  source: ReferenceRankingSource,
  term: RegularTerm?,
  exchange: PortalAcademicExchange,
  studentNumber: String,
): JsonObject {
  val status = exchange.httpStatus
  val text = exchange.bodyText
  if (exchange.transportError != null || status == null || text == null) {
    return sourceResponse(source, term, ReferenceRankingFailure.NETWORK_ERROR, httpStatus = null)
  }
  if (status !in 200..299) return sourceResponse(source, term, ReferenceRankingFailure.HTTP_ERROR, status)

  val invalid = sourceResponse(source, term, ReferenceRankingFailure.INVALID_RESPONSE, status)
  // 로그인 HTML처럼 JSON이 아닌 응답은 HTTP 200이어도 실패다.
  val root = exchange.receivedObjectOrNull() ?: return invalid
  val apiErrorCode = root.plainText("errorCode")
  if (!apiErrorCode.isNullOrBlank()) {
    val failure = if (apiErrorCode == SESSION_EXPIRED_API_CODE) ReferenceRankingFailure.SESSION_EXPIRED else ReferenceRankingFailure.INVALID_RESPONSE
    return sourceResponse(source, term, failure, status)
  }
  val body = if (source.allowedFields != null) root.projected(source) ?: return invalid else root
  val isEmpty = body.targetIsEmptyOrNull(source, term) ?: return invalid
  if (!body.studentNumbersMatch(studentNumber)) return invalid
  // 성적·수강 응답은 숫자 정밀도와 null을 지키도록 받은 텍스트를 그대로 보낸다.
  val responseBody = if (source.allowedFields != null) body.toString() else text
  if (responseBody.length > MAX_BODY_LENGTH) return invalid

  return buildJsonObject {
    put("source", source.name)
    put("year", term?.takeIf { source.hasTerm }?.year)
    put("semester", term?.takeIf { source.hasTerm }?.semester)
    put("status", if (isEmpty) "EMPTY" else "SUCCESS")
    put("httpStatus", status)
    put("responseBody", responseBody)
    put("errorCode", JsonNull)
  }
}

internal enum class ReferenceRankingFailure { NETWORK_ERROR, SESSION_EXPIRED, HTTP_ERROR, INVALID_RESPONSE }

private fun sourceResponse(
  source: ReferenceRankingSource,
  term: RegularTerm?,
  failure: ReferenceRankingFailure,
  httpStatus: Int?,
) = buildJsonObject {
  put("source", source.name)
  put("year", term?.takeIf { source.hasTerm }?.year)
  put("semester", term?.takeIf { source.hasTerm }?.semester)
  put("status", "FAILED")
  put("httpStatus", httpStatus)
  put("responseBody", JsonNull)
  put("errorCode", failure.name)
}

private fun PortalAcademicExchange.receivedObjectOrNull(): JsonObject? {
  if (transportError != null || httpStatus !in 200..299) return null
  return try {
    Json.parseToJsonElement(bodyText ?: return null) as? JsonObject
  } catch (e: SerializationException) {
    null
  }
}

/** 학적 응답에서 대상 필드만 남기고, 그 안의 행에서도 허용 필드만 남긴다. 대상 필드가 없으면 null이다. */
private fun JsonObject.projected(source: ReferenceRankingSource): JsonObject? {
  val allowed = source.allowedFields ?: return this
  if (source.targetField !in this) return null
  fun JsonElement.retained(): JsonElement = if (this is JsonObject) JsonObject(filterKeys { it in allowed }) else this
  val target = when (val value = getValue(source.targetField)) {
    is JsonArray -> JsonArray(value.map { it.retained() })
    else -> value.retained()
  }
  return JsonObject(mapOf(source.targetField to target))
}

/**
 * 대상 필드의 형식과 조회 학기를 확인한다. 정상 빈 결과면 true, 자료가 있으면 false, 서버 계약에 맞지 않으면 null이다.
 */
private fun JsonObject.targetIsEmptyOrNull(source: ReferenceRankingSource, term: RegularTerm?): Boolean? {
  val target = this[source.targetField] ?: return null
  if (source == ReferenceRankingSource.MAJOR_ASSIGNMENT) {
    return when (target) {
      is JsonNull -> true
      is JsonObject -> when {
        target.isEmpty() -> true
        term != null && target.matchesTerm("applYear", "applSmrCd", term) -> false
        else -> null
      }

      else -> null
    }
  }
  val rows = target as? JsonArray ?: return null
  if (rows.size > MAX_ROWS || rows.any { it !is JsonObject }) return null
  if (source.hasTerm) {
    if (term == null) return null
    val (yearKey, semesterKey) = when (source) {
      ReferenceRankingSource.GRADE_DETAIL -> "cretGainYear" to "cretSmrCd"
      else -> "subjtEstbYear" to "subjtEstbSmrCd"
    }
    if (!rows.all { (it as JsonObject).matchesTerm(yearKey, semesterKey, term) }) return null
  }
  return rows.isEmpty()
}

/** 값이 있으면 조회 학기와 같아야 한다. 서버는 숫자 문자열이나 정수만 받는다. */
private fun JsonObject.matchesTerm(yearKey: String, semesterKey: String, term: RegularTerm): Boolean =
  matchesNumber(yearKey, term.year) && matchesNumber(semesterKey, term.semester)

private fun JsonObject.matchesNumber(key: String, expected: Int): Boolean {
  val value = this[key]
  if (value == null || value is JsonNull) return true
  val primitive = value as? JsonPrimitive ?: return false
  val isIntegral = primitive.isString || INTEGER.matches(primitive.content)
  return isIntegral && primitive.content == expected.toString()
}

private val INTEGER = Regex("-?[0-9]+")

/** 응답 어디에 있든 `sno`는 업로드한 학생의 학번 문자열이어야 한다. */
private fun JsonElement.studentNumbersMatch(studentNumber: String): Boolean = when (this) {
  is JsonObject -> {
    val sno = this["sno"]
    val matches = sno == null || sno is JsonNull || (sno is JsonPrimitive && sno.isString && sno.content == studentNumber)
    matches && values.all { it.studentNumbersMatch(studentNumber) }
  }

  is JsonArray -> all { it.studentNumbersMatch(studentNumber) }
  else -> true
}

/** 문자열이나 숫자 값을 앞뒤 공백 없이 읽는다. 서버 정규화의 `text()`와 같다. */
private fun JsonObject.plainText(key: String): String? =
  (this[key] as? JsonPrimitive)?.takeUnless { it is JsonNull }?.takeIf { it.isString || it.content.toDoubleOrNull() != null }
    ?.content?.trim()

private fun termBody(studentId: String, yearKey: String, semesterKey: String, term: RegularTerm) = buildJsonObject {
  put("sno", studentId)
  put(yearKey, term.year.toString())
  put(semesterKey, term.semester.toString())
}
