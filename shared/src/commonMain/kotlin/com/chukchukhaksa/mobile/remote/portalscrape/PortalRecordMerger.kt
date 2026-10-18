package com.chukchukhaksa.mobile.remote.portalscrape

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * 성적(credit)과 수강(course) 데이터를 학기별로 병합한다. suwon-scraper-v1 `services/merge.ts`와 같은 규칙이다.
 *
 * - 학기 키는 `{year}-{semesterCode}`이고, 같은 학기 안에서는 과목 코드(subjtCd)로 병합한다.
 * - point는 수강 데이터의 non-null point를 우선하고, 없으면 성적 gainPoint로 보정한다.
 * - 객체 속성은 포털 원본 필드를 모두 보존하며 순서도 JS의 스프레드·Object.assign 결과와 같다.
 */
internal fun mergeCreditCourse(creditDTOs: List<JsonObject>, courseDTOs: List<JsonObject>): JsonArray {
  val semesterMap = JsRecord<MergedSemester>()
  // 과목 코드별 수강 데이터의 원래 point. 값이 Kotlin null이면 수강 데이터에 point 키가 없던 것(undefined)이다.
  val originalCoursePoints = JsRecord<JsRecord<JsonElement?>>()

  for (course in courseDTOs) {
    val semesterKey = "${course["subjtEstbYear"].toJsString()}-${course["subjtEstbSmrCd"].toJsString()}"
    val semester = semesterMap.getOrPut(semesterKey) { MergedSemester(semesterKey) }
    val coursePoints = originalCoursePoints.getOrPut(semesterKey) { JsRecord() }
    val subjectCode = course["subjtCd"].toJsString()
    semester.courses[subjectCode] = LinkedHashMap(course)
    coursePoints[subjectCode] = course["point"]
  }

  for (credit in creditDTOs) {
    val semesterKey = "${credit["cretGainYear"].toJsString()}-${credit["cretSmrCd"].toJsString()}"
    val semester = semesterMap.getOrPut(semesterKey) { MergedSemester(semesterKey) }
    val subjectCode = credit["subjtCd"].toJsString()
    val existing = semester.courses[subjectCode]
    val gainPoint = credit["gainPoint"]

    if (existing != null) {
      // 같은 과목이 이미 있으면 수강 데이터에 성적 데이터를 덮어쓴다(Object.assign).
      val coursePoints = originalCoursePoints[semesterKey]
      val hasOriginalCourse = coursePoints?.containsKey(subjectCode) == true
      val originalCoursePoint = coursePoints?.get(subjectCode)
      existing.putAll(credit)
      if (!credit.containsKey("gainPoint")) {
        existing.remove("gainPoint")
      }
      when {
        hasOriginalCourse && !originalCoursePoint.isNullish() -> existing["point"] = originalCoursePoint!!
        !gainPoint.isNullish() -> existing["point"] = gainPoint!!
        hasOriginalCourse -> existing["point"] = JsonNull
        else -> existing.remove("point")
      }
    } else {
      // 수강 데이터가 없으면 성적 데이터만 추가한다.
      val merged = LinkedHashMap(credit)
      if (!gainPoint.isNullish()) {
        merged["point"] = gainPoint!!
      }
      semester.courses[subjectCode] = merged
    }
  }

  return JsonArray(
    semesterMap.values().map { semester ->
      JsonObject(
        mapOf(
          "semester" to JsonPrimitive(semester.key),
          "courses" to JsonArray(semester.courses.values().map(::JsonObject)),
        ),
      )
    },
  )
}

private class MergedSemester(val key: String) {
  val courses = JsRecord<LinkedHashMap<String, JsonElement>>()
}
