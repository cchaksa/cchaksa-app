package com.chukchukhaksa.mobile.remote.portalscrape

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

// suwon-scraper-v1(TypeScript)의 결과 JSON을 그대로 재현하려고 필요한 JS 동작만 옮긴 보조 함수들.
// Kotlin null은 JS undefined(키 없음), JsonNull은 JS null에 대응한다.

/** 템플릿 리터럴(`${value}`)이 값을 문자열로 바꾸는 결과. */
internal fun JsonElement?.toJsString(): String = when (this) {
  null -> "undefined"
  JsonNull -> "null"
  is JsonPrimitive -> if (isString) content else content.toJsNumberString()
  is JsonObject -> "[object Object]"
  is JsonArray -> joinToString(",") { if (it is JsonNull) "" else it.toJsString() }
}

private fun String.toJsNumberString(): String {
  val number = toDoubleOrNull() ?: return this
  val isSafeInteger = number % 1.0 == 0.0 && number in -MAX_SAFE_INTEGER..MAX_SAFE_INTEGER
  return if (isSafeInteger) number.toLong().toString() else this
}

private const val MAX_SAFE_INTEGER = 9_007_199_254_740_991.0

/** JS의 `value == null` (null 또는 undefined). */
internal fun JsonElement?.isNullish(): Boolean = this == null || this is JsonNull

/** JS 진릿값 판정. `value || []` 처럼 빈 값을 기본값으로 바꾸는 코드에 쓴다. */
internal fun JsonElement?.isJsTruthy(): Boolean = when (this) {
  null, JsonNull -> false
  is JsonPrimitive -> when {
    isString -> content.isNotEmpty()
    booleanOrNull != null -> booleanOrNull == true
    else -> doubleOrNull.let { it != null && it != 0.0 && !it.isNaN() }
  }
  else -> true
}

/** `parseInt(value) || 0` 과 같은 결과. */
internal fun jsParseIntOrZero(value: JsonElement?): Int {
  val text = (value as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content ?: return 0
  return LEADING_INTEGER.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
}

private val LEADING_INTEGER = Regex("""^\s*([+-]?\d+)""")

/**
 * JS 객체를 키-값 저장소로 쓸 때의 열거 순서(`Object.values`)를 재현한다.
 * 배열 인덱스 형태의 키("0", "123")가 숫자 오름차순으로 먼저 오고, 나머지 키는 삽입 순서를 따른다.
 * 과목 코드가 "12345"처럼 숫자로만 이뤄지면 이 규칙 때문에 스크래퍼 결과의 과목 순서가 달라진다.
 */
internal class JsRecord<V> {
  private val entries = LinkedHashMap<String, V>()

  operator fun get(key: String): V? = entries[key]

  operator fun set(key: String, value: V) {
    entries[key] = value
  }

  fun containsKey(key: String): Boolean = entries.containsKey(key)

  fun getOrPut(key: String, defaultValue: () -> V): V = entries.getOrPut(key, defaultValue)

  fun values(): List<V> {
    val (indexKeys, otherKeys) = entries.keys.partition { it.isArrayIndexKey() }
    return (indexKeys.sortedBy { it.toLong() } + otherKeys).map { entries.getValue(it) }
  }
}

private fun String.isArrayIndexKey(): Boolean {
  if (isEmpty() || length > 10 || any { it !in '0'..'9' }) return false
  if (length > 1 && this[0] == '0') return false
  return toLong() < 4_294_967_295L
}
