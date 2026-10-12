package com.chukchukhaksa.mobile.common.ui

import androidx.compose.ui.graphics.Color
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray500
import com.chukchukhaksa.mobile.common.designsystem.theme.Green200
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.Red400
import com.chukchukhaksa.mobile.common.designsystem.theme.Yellow200

fun String.gradeColor(): Color {
  return when(this) {
    "A+", "A0" -> return Red400
    "B+", "B0" -> return Yellow200
    "C+", "C0" -> return Green200
    "D+", "D0", "F" -> return Gray500
    "P" -> return Purple600
    else -> Black100
  }
}
