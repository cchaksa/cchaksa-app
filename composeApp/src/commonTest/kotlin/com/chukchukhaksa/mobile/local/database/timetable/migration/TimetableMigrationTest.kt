package com.chukchukhaksa.mobile.local.database.timetable.migration

import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.local.database.timetable.converter.TimetableCellListConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 마이그레이션의 SQL 실행 부분은 실제 데이터베이스가 필요해서 여기서 다루지 않고, 레거시
 * `timeTableJsonData` 를 새 `cellList` 로 옮기는 변환 규칙만 검증한다.
 */
class TimetableMigrationTest {

  private val converter = TimetableCellListConverter()

  /** 버전 1 시절 실제로 저장되던 형태의 데이터이다. */
  private val legacyJsonData = """
    [
      {"color":-9728172,"credit":"3","day":"목","endTime":"4","location":"미래520","name":"도전과창조-기업가정신","professor":"김기선","startTime":"3"},
      {"color":-4026526,"credit":"","day":"화","endTime":"6","location":"미래520","name":"도전과창조-기업가정신","professor":"홍태민","startTime":"5"},
      {"color":-96120,"credit":"","day":"토","endTime":"4","location":"인문407","name":"언어와문화","professor":"김동섭","startTime":"3"},
      {"color":-12363882,"credit":"","day":"","endTime":"","location":"이러닝","name":"도전과창조-기업가정신","professor":"김기선","startTime":""}
    ]
  """.trimIndent()

  @Test
  fun `레거시 JSON 을 변환한 결과는 Room 컨버터가 그대로 읽는다`() {
    val converted = timeTableJsonDataToCellList(legacyJsonData)

    val cells = converter.jsonToCellList(converted)

    assertEquals(4, cells.size)
    assertEquals("도전과창조-기업가정신", cells[0].name)
    assertEquals("김기선", cells[0].professor)
    assertEquals("미래520", cells[0].location)
  }

  @Test
  fun `요일 문자열을 열거형으로 옮기고 값이 비어 있으면 이러닝으로 본다`() {
    val cells = converter.jsonToCellList(timeTableJsonDataToCellList(legacyJsonData))

    assertEquals(TimetableDay.THU, cells[0].day)
    assertEquals(TimetableDay.TUE, cells[1].day)
    assertEquals(TimetableDay.SAT, cells[2].day)
    assertEquals(TimetableDay.E_LEARNING, cells[3].day)
  }

  @Test
  fun `교시는 정수로 옮기고 값이 비어 있으면 0 으로 둔다`() {
    val cells = converter.jsonToCellList(timeTableJsonDataToCellList(legacyJsonData))

    assertEquals(3, cells[0].startPeriod)
    assertEquals(4, cells[0].endPeriod)
    assertEquals(0, cells[3].startPeriod)
    assertEquals(0, cells[3].endPeriod)
  }

  @Test
  fun `레거시 색상 코드를 새 색상 열거형으로 옮긴다`() {
    val cells = converter.jsonToCellList(timeTableJsonDataToCellList(legacyJsonData))

    assertEquals(TimetableCellColor.GREEN_DARK, cells[0].color)
    assertEquals(TimetableCellColor.BROWN, cells[1].color)
    assertEquals(TimetableCellColor.PINK, cells[2].color)
    assertEquals(TimetableCellColor.NAVY, cells[3].color)
  }

  @Test
  fun `학점 값을 잃지 않고 함께 옮긴다`() {
    val cells = converter.jsonToCellList(timeTableJsonDataToCellList(legacyJsonData))

    assertEquals("3", cells[0].credit)
  }

  @Test
  fun `알 수 없는 필드가 섞여 있어도 변환한다`() {
    val jsonWithUnknownField = """
      [{"color":-96120,"credit":"","day":"월","endTime":"2","location":"인문407",
        "name":"언어와문화","professor":"김동섭","startTime":"1","legacyOnlyField":"무시 대상"}]
    """.trimIndent()

    val cells = converter.jsonToCellList(timeTableJsonDataToCellList(jsonWithUnknownField))

    assertEquals(1, cells.size)
    assertEquals(TimetableDay.MON, cells[0].day)
  }

  @Test
  fun `매핑에 없는 색상 코드는 임의의 색상으로 대체한다`() {
    val jsonWithUnknownColor = """
      [{"color":12345,"credit":"","day":"금","endTime":"2","location":"인문407",
        "name":"언어와문화","professor":"김동섭","startTime":"1"}]
    """.trimIndent()

    val cells = converter.jsonToCellList(timeTableJsonDataToCellList(jsonWithUnknownColor))

    assertEquals(1, cells.size)
    assertTrue(cells[0].color in TimetableCellColor.entries)
  }

  @Test
  fun `깨진 JSON 은 예외를 던지지 않고 빈 목록으로 남긴다`() {
    val broken = """[{"color":-96120,"day":"월","""

    val converted = timeTableJsonDataToCellList(broken)

    assertEquals("", converted)
    assertTrue(converter.jsonToCellList(converted).isEmpty())
  }

  @Test
  fun `셀이 하나도 없는 시간표는 변환 실패와 구분된다`() {
    // 마이그레이션은 "원본이 비어 있지 않은데 결과가 비었다"는 조건으로 변환 실패를 판정한다.
    // 빈 배열은 정상 변환이므로 결과도 비어 있으면 안 된다.
    val converted = timeTableJsonDataToCellList("[]")

    assertTrue(converted.isNotBlank())
    assertTrue(converter.jsonToCellList(converted).isEmpty())
  }

  @Test
  fun `빈 입력은 빈 목록으로 남긴다`() {
    assertEquals("", timeTableJsonDataToCellList(""))
    assertEquals("", timeTableJsonDataToCellList("   "))
  }
}
