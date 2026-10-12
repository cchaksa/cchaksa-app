package com.chukchukhaksa.mobile.common.ui


import kotlinx.serialization.json.Json

inline fun <reified T> Json.encodeToUri(value: T): String {
  return encodeToString(value).uriEncode()
}

inline fun <reified T> Json.decodeFromUri(value: String): T {
  return decodeFromString(value.uriDecode())
}

fun String.uriEncode(): String = buildString {
  for (c in this@uriEncode) {
    if (c.isLetterOrDigit() || c in listOf('-', '_', '.', '~')) {
      append(c)
    } else {
      append("%")
      append(c.code.toString(16).uppercase().padStart(2, '0'))
    }
  }
}

fun String.uriDecode(): String {
  val result = StringBuilder()
  var i = 0
  while (i < this.length) {
    if (this[i] == '%' && i + 2 < this.length) {
      val hex = this.substring(i + 1, i + 3)
      result.append(hex.toInt(16).toChar())
      i += 3
    } else {
      result.append(this[i])
      i++
    }
  }
  return result.toString()
}