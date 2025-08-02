package com.chukchukhaksa.mobile.common.model

// TODO
/*
enum 변수 값을 CellColor1, CellColor2 ... 으로 변경 필요
시간표 색상이 변경되었을 때, enum 변수 값이 변경되게 된다면 DB에도 영향이 가는 구조.
 */
enum class TimetableCellColor {
  BROWN,
  BROWN_LIGHT,
  ORANGE,
  APRICOT, // 살구
  PURPLE,
  PURPLE_LIGHT,
  RED_LIGHT,
  PINK,
  BROWN_DARK,
  GREEN_DARK,
  GREEN,
  GREEN_LIGHT,
  NAVY_DARK,
  NAVY,
  NAVY_LIGHT,
  VIOLET,
  GRAY_DARK,
  GRAY,
  SKY,
  VIOLET_LIGHT,
}

enum class TimetableDay(val idx: Int) {
  MON(0), TUE(1), WED(2), THU(3), FRI(4), SAT(5), E_LEARNING(6)
}
