package com.chukchukhaksa.mobile.common.extension

fun Int.toAcademicTerm(): String {
  if (this !in 1..8) return "알 수 없음"

  val grade = (this + 1) / 2
  val semester = if (this % 2 != 0) 1 else 2

  return "${grade}학년 ${semester}학기"
}
