package com.chukchukhaksa.mobile.remote.portalscrape

import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeErrorCode
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeException
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/** 학사 시스템 조회. suwon-scraper-v1의 crawlers 디렉터리와 1:1로 대응하며 포털 응답 필드를 그대로 보존한다. */
internal interface PortalCrawlers {
  suspend fun scrapeStudent(studentId: String): JsonObject
  suspend fun scrapeCourses(studentId: String): List<JsonObject>
  suspend fun scrapeCredits(studentId: String): PortalCreditResult
  suspend fun scrapeDesignatedCourses(studentId: String): JsonArray
}

internal class PortalCreditResult(
  val creditDTOs: List<JsonObject>,
  val gradeResponse: JsonObject,
  val warnings: List<String> = emptyList(),
)

private const val STUDENT_PATH = "/scrgBas/selectScrgBas.do"
private const val COURSE_SEMESTERS_PATH = "/atlecApplDtai/listAtlecApplDtaiTabYearSmr.do"
private const val COURSE_DETAIL_PATH = "/atlecApplDtai/listAtlecApplDtaiTabSubjt.do"
private const val GRADE_SUMMARY_PATH = "/smrCretSum/listSmrCretSumTabYearSmrStud.do"
private const val CREDIT_DETAIL_PATH = "/cretBas/listSmrCretSumTabSubjt.do"
private const val DESIGNATED_COURSE_PATH = "/precpSbjt/listPrecpSbjt.do"

// 학부 전용 조회 코드. 근거: https://info.suwon.ac.kr/js/sa/commSa.js 의 openSubjtPopUp.
private const val UNDERGRADUATE_ORG_CLASS_CODE = "20"

// studentCrawler.ts가 학생 정보 응답에서 골라 담는 필드와 순서.
private val STUDENT_FIELDS = listOf(
  "sno", "studNm", "univCd", "univNm", "dpmjCd", "dpmjNm", "mjorCd", "mjorNm", "the2MjorCd", "the2MjorNm",
  "enscYear", "enscSmrCd", "scrgStatNm", "studGrde", "enscDvcd", "facSmrCnt", "flangPassGb",
)

// creditCrawler.ts가 성적 합계가 없을 때 채우는 기본값.
private val DEFAULT_GRADE_TOTAL = buildJsonObject {
  put("gainPoint", "0")
  put("applPoint", "0")
  put("gainAvmk", "0")
  put("gainTavgPont", "0")
}

private val ACADEMIC_JSON = ContentType.parse("application/json;charset=UTF-8")

internal class HttpPortalCrawlers(
  private val client: HttpClient,
) : PortalCrawlers {

  override suspend fun scrapeStudent(studentId: String): JsonObject {
    val root = postAcademicApi(STUDENT_PATH, snoBody(studentId), label = "학생 정보").requireObject("학생 정보")
    val studentInfo = root["studentInfo"] as? JsonObject
      ?: throw schemaMismatch("학생 정보 응답에 studentInfo가 없습니다.")
    return mapStudent(studentInfo)
  }

  override suspend fun scrapeCourses(studentId: String): List<JsonObject> {
    val semesters = postAcademicApi(COURSE_SEMESTERS_PATH, snoBody(studentId), label = "수강 학기 목록")
      .requireObject("수강 학기 목록")["listAtlecApplDtaiTabYearSmr"]
      .arrayOrEmpty("수강 학기 목록")

    val courses = mutableListOf<JsonObject>()
    for (semester in semesters.filterIsInstance<JsonObject>()) {
      val body = buildJsonObject {
        put("sno", studentId)
        semester["subjtEstbYear"]?.let { put("subjtEstbYear", it) }
        semester["subjtEstbSmrCd"]?.let { put("subjtEstbSmrCd", it) }
      }
      courses += postAcademicApi(COURSE_DETAIL_PATH, body, label = "수강 상세")
        .requireObject("수강 상세")["listAtlecApplDtaiTabSubjt"]
        .arrayOrEmpty("수강 상세")
        .filterIsInstance<JsonObject>()
    }
    return courses
  }

  override suspend fun scrapeCredits(studentId: String): PortalCreditResult {
    val gradeData = postAcademicApi(GRADE_SUMMARY_PATH, snoBody(studentId), label = "성적 요약").requireObject("성적 요약")
    val semesterList = gradeData["listSmrCretSumTabYearSmr"]
    val total = gradeData["selectSmrCretSumTabSjTotal"]

    // 신입생: 성적 합계나 학기 목록이 없으면 기본 성적 구조를 돌려준다.
    val isNewStudent = !total.isJsTruthy() || !semesterList.isJsTruthy() || (semesterList as? JsonArray)?.isEmpty() == true
    if (isNewStudent) {
      return PortalCreditResult(
        creditDTOs = emptyList(),
        gradeResponse = buildJsonObject {
          put("listSmrCretSumTabYearSmr", semesterList as? JsonArray ?: JsonArray(emptyList()))
          put("selectSmrCretSumTabSjTotal", DEFAULT_GRADE_TOTAL)
        },
      )
    }

    val gradeResponse = LinkedHashMap(gradeData)
    val totalObject = total as? JsonObject ?: throw schemaMismatch("성적 요약 응답의 selectSmrCretSumTabSjTotal 형식이 올바르지 않습니다.")
    if (totalObject["gainPoint"].isNullish()) {
      gradeResponse["selectSmrCretSumTabSjTotal"] = JsonObject(LinkedHashMap(totalObject).apply { put("gainPoint", JsonPrimitive("0")) })
    }
    if (semesterList !is JsonArray) {
      gradeResponse["listSmrCretSumTabYearSmr"] = JsonArray(emptyList())
      return PortalCreditResult(creditDTOs = emptyList(), gradeResponse = JsonObject(gradeResponse))
    }

    val creditDTOs = mutableListOf<JsonObject>()
    val warnings = mutableListOf<String>()
    for (item in semesterList) {
      val semester = item as? JsonObject ?: continue
      val year = semester["cretGainYear"]
      val semesterCode = semester["cretSmrCd"]
      val label = "${year.toJsString()}-${semesterCode.toJsString()}"
      // 스크래퍼처럼 학기 하나의 실패는 건너뛰고 다음 학기를 계속 조회한다.
      try {
        val body = buildJsonObject {
          put("sno", studentId)
          year?.let { put("cretGainYear", it) }
          semesterCode?.let { put("cretSmrCd", it) }
        }
        val entries = postAcademicApi(CREDIT_DETAIL_PATH, body, label = "성적 상세").requireObject("성적 상세")["listSmrCretSumTabSubjt"]
        if (!entries.isJsTruthy()) {
          warnings += "$label 학기 성적 상세가 비어 있어 건너뛰었습니다."
          continue
        }
        val entryList = entries as? JsonArray ?: throw schemaMismatch("성적 상세 응답의 listSmrCretSumTabSubjt가 배열이 아닙니다.")
        creditDTOs += entryList.filterIsInstance<JsonObject>().map { entry ->
          JsonObject(
            LinkedHashMap(entry).apply {
              putOrRemove("cretGainYear", year)
              putOrRemove("cretSmrCd", semesterCode)
            },
          )
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Napier.w(tag = LOG_TAG) { "credit detail skipped: $label (${e.message})" }
        warnings += "$label 학기 성적 상세 조회에 실패해 건너뛰었습니다. (${e.message})"
      }
    }
    return PortalCreditResult(creditDTOs = creditDTOs, gradeResponse = JsonObject(gradeResponse), warnings = warnings)
  }

  override suspend fun scrapeDesignatedCourses(studentId: String): JsonArray {
    val body = buildJsonObject {
      put("sno", studentId)
      put("orgClsCd", UNDERGRADUATE_ORG_CLASS_CODE)
    }
    val data = postAcademicApi(DESIGNATED_COURSE_PATH, body, label = "지정과목")
    // 편입생의 정상 빈 응답은 명시적인 빈 배열이다. 키 누락·null·배열이 아닌 값은 빈 목록으로 대체하지 않는다.
    return (data as? JsonObject)?.get("precpSbjtList") as? JsonArray
      ?: throw schemaMismatch("지정과목 응답의 precpSbjtList가 배열이 아닙니다.")
  }

  private suspend fun postAcademicApi(path: String, body: JsonObject, label: String): JsonElement {
    val response = client.post(SuwonPortalUrls.ACADEMIC_BASE + path) {
      header(HttpHeaders.Accept, "application/json")
      header(HttpHeaders.Referrer, SuwonPortalUrls.ACADEMIC_REFERER)
      setBody(TextContent(body.toString(), ACADEMIC_JSON))
    }
    Napier.d(tag = LOG_TAG) { "POST $path → ${response.status.value}" }
    if (!response.status.isSuccess()) {
      val status = response.status.value
      if (status in 500..599) {
        throw PortalScrapeException(
          errorCode = PortalScrapeErrorCode.PORTAL_TEMPORARY_UNAVAILABLE,
          message = "$label 조회에 실패했습니다. (HTTP $status)",
          retryable = true,
        )
      }
      throw PortalScrapeException(PortalScrapeErrorCode.UNKNOWN_NON_RETRYABLE, "$label 조회에 실패했습니다. (HTTP $status)", false)
    }

    val element = try {
      Json.parseToJsonElement(response.bodyAsText())
    } catch (e: SerializationException) {
      throw schemaMismatch("$label 응답을 JSON으로 해석할 수 없습니다.", e)
    }
    (element as? JsonObject)?.academicApiErrorOrNull()?.let { throw it }
    return element
  }
}

private fun snoBody(studentId: String) = buildJsonObject { put("sno", studentId) }

internal fun mapStudent(studentInfo: JsonObject): JsonObject = buildJsonObject {
  STUDENT_FIELDS.forEach { key ->
    if (key == "facSmrCnt") {
      put(key, jsParseIntOrZero(studentInfo[key]))
    } else {
      // 응답에 없는 필드는 스크래퍼(undefined → JSON 생략)처럼 넣지 않는다.
      studentInfo[key]?.let { put(key, it) }
    }
  }
}

/** 세션이 없으면 학사 API는 200과 함께 `{"errorCode":"-998","errorMessage":"로그인 하지 않았거나, 세션이 종료되었습니다."}`를 준다. */
private fun JsonObject.academicApiErrorOrNull(): PortalScrapeException? {
  val code = (this["errorCode"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
  val message = (this["errorMessage"] as? JsonPrimitive)?.contentOrNull ?: "메시지 없음"
  return PortalScrapeException(
    errorCode = PortalScrapeErrorCode.BUSINESS_RULE_VIOLATION,
    message = "학사 시스템이 오류를 반환했습니다. ($message, errorCode=$code)",
    retryable = false,
  )
}

private fun JsonElement.requireObject(label: String): JsonObject =
  this as? JsonObject ?: throw schemaMismatch("$label 응답 형식이 올바르지 않습니다.")

/** 스크래퍼의 `list || []` 처리. 빈 값은 빈 목록으로 보고, 배열이 아닌 값은 형식 오류로 본다. */
private fun JsonElement?.arrayOrEmpty(label: String): JsonArray = when {
  this is JsonArray -> this
  !isJsTruthy() -> JsonArray(emptyList())
  else -> throw schemaMismatch("$label 응답의 목록이 배열이 아닙니다.")
}

private fun LinkedHashMap<String, JsonElement>.putOrRemove(key: String, value: JsonElement?) {
  if (value == null) remove(key) else put(key, value)
}

private fun schemaMismatch(message: String, cause: Throwable? = null) = PortalScrapeException(
  errorCode = PortalScrapeErrorCode.PORTAL_RESPONSE_SCHEMA_MISMATCH,
  message = message,
  retryable = false,
  cause = cause,
)
