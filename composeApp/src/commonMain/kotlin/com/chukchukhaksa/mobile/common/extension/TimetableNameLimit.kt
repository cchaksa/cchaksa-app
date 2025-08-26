package com.chukchukhaksa.mobile.common.extension

const val TIMETABLE_NAME_LIMIT = 20

fun checkOverTimetableNameLimit(name: String): Boolean {
    return name.trim().length > TIMETABLE_NAME_LIMIT
}

fun checkTimetableNameRule(name: String): Boolean {
    return name.isNotBlank() && name.trim().length <= TIMETABLE_NAME_LIMIT
}
