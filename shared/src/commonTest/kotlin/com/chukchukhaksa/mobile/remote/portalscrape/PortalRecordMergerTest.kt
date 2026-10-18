package com.chukchukhaksa.mobile.remote.portalscrape

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// suwon-scraper-v1 src/tests/merge.spec.ts 를 옮긴 테스트.
class PortalRecordMergerTest {

  private fun credit(block: JsonObjectBuilder.() -> Unit = {}) = buildJsonObject {
    put("subjtCd", "04048")
    put("cretGainYear", "2025")
    put("cretSmrCd", "10")
    block()
  }

  private fun course(point: Int?) = buildJsonObject {
    put("subjtCd", "04048")
    put("subjtEstbYear", 2025)
    put("subjtEstbSmrCd", "10")
    put("point", point)
  }

  private fun firstCourse(credits: List<JsonObject>, courses: List<JsonObject> = emptyList()): JsonObject =
    mergeCreditCourse(credits, courses)[0].jsonObject["courses"]!!.jsonArray[0].jsonObject

  @Test
  fun `point가 null이면 gainPoint를 학점으로 사용한다`() {
    val merged = firstCourse(listOf(credit { put("gainPoint", 3) }), listOf(course(point = null)))

    assertEquals(JsonPrimitive(3), merged["point"])
  }

  @Test
  fun `수강 데이터 없이 성적 데이터만 있으면 gainPoint를 point로 사용한다`() {
    assertEquals(JsonPrimitive(3), firstCourse(listOf(credit { put("gainPoint", 3) }))["point"])
  }

  @Test
  fun `성적 데이터의 gainPoint가 0이면 point에 0을 유지한다`() {
    assertEquals(JsonPrimitive(0), firstCourse(listOf(credit { put("gainPoint", 0) }))["point"])
  }

  @Test
  fun `성적 데이터의 gainPoint가 없으면 point를 생성하지 않는다`() {
    assertFalse(firstCourse(listOf(credit())).containsKey("point"))
  }

  @Test
  fun `성적 단독 데이터가 중복되면 최신 gainPoint를 point로 사용한다`() {
    val merged = firstCourse(listOf(credit { put("gainPoint", 2) }, credit { put("gainPoint", 3) }))

    assertEquals(JsonPrimitive(3), merged["gainPoint"])
    assertEquals(JsonPrimitive(3), merged["point"])
  }

  @Test
  fun `성적 데이터의 gainPoint가 명시적 null이면 point를 생성하지 않는다`() {
    assertFalse(firstCourse(listOf(credit { put("gainPoint", JsonNull) })).containsKey("point"))
  }

  @Test
  fun `수강 데이터의 non-null point는 중복 성적보다 우선한다`() {
    val merged = firstCourse(listOf(credit { put("gainPoint", 2) }, credit { put("gainPoint", 3) }), listOf(course(point = 4)))

    assertEquals(JsonPrimitive(4), merged["point"])
  }

  @Test
  fun `수강 데이터의 point가 null이면 최신 gainPoint를 point로 사용한다`() {
    val merged = firstCourse(listOf(credit { put("gainPoint", 2) }, credit { put("gainPoint", 3) }), listOf(course(point = null)))

    assertEquals(JsonPrimitive(3), merged["point"])
  }

  @Test
  fun `성적 단독 데이터의 최신 gainPoint가 null이면 이전 point를 제거한다`() {
    val merged = firstCourse(listOf(credit { put("gainPoint", 2) }, credit { put("gainPoint", JsonNull) }))

    assertFalse(merged.containsKey("point"))
  }

  @Test
  fun `성적 단독 데이터의 최신 입력에 gainPoint 키가 없으면 이전 gainPoint와 point를 제거한다`() {
    val merged = firstCourse(listOf(credit { put("gainPoint", 2) }, credit()))

    assertFalse(merged.containsKey("gainPoint"))
    assertFalse(merged.containsKey("point"))
  }

  @Test
  fun `수강 데이터의 원래 point가 null이고 최신 gainPoint가 null이면 null을 보존한다`() {
    val merged = firstCourse(listOf(credit { put("gainPoint", 2) }, credit { put("gainPoint", JsonNull) }), listOf(course(point = null)))

    assertEquals(JsonNull, merged["point"])
  }

  @Test
  fun `수강 원본 필드를 보존하고 성적 필드는 뒤에 이어 붙인다`() {
    val merged = firstCourse(listOf(credit { put("cretGrdCd", "A+"); put("gainPoint", 3) }), listOf(course(point = 3)))

    assertEquals(
      listOf("subjtCd", "subjtEstbYear", "subjtEstbSmrCd", "point", "cretGainYear", "cretSmrCd", "cretGrdCd", "gainPoint"),
      merged.keys.toList(),
    )
  }

  @Test
  fun `학기와 과목 순서는 JS 객체 열거 순서를 따른다`() {
    val credits = listOf("A100", "12345", "04048", "2").map { code ->
      buildJsonObject {
        put("subjtCd", code)
        put("cretGainYear", "2025")
        put("cretSmrCd", "10")
      }
    } + buildJsonObject {
      put("subjtCd", "X")
      put("cretGainYear", "2024")
      put("cretSmrCd", "20")
    }

    val semesters = mergeCreditCourse(credits, emptyList())

    assertEquals(listOf("2025-10", "2024-20"), semesters.map { it.jsonObject["semester"]!!.jsonPrimitive.content })
    assertEquals(
      // 배열 인덱스 형태의 키("2", "12345")가 숫자 순으로 먼저 오고, "04048"처럼 0으로 시작하는 키는 삽입 순서를 따른다.
      listOf("2", "12345", "A100", "04048"),
      semesters[0].jsonObject["courses"]!!.jsonArray.map { it.jsonObject["subjtCd"]!!.jsonPrimitive.content },
    )
  }
}
