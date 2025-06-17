package com.chukchukhaksa.mobile.presentation.timetable.openlecture.model

import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.all_timetable_cell_screen_grade_1
import chukchukhaksa.composeapp.generated.resources.all_timetable_cell_screen_grade_2
import chukchukhaksa.composeapp.generated.resources.all_timetable_cell_screen_grade_3
import chukchukhaksa.composeapp.generated.resources.all_timetable_cell_screen_grade_4
import chukchukhaksa.composeapp.generated.resources.word_all
import org.jetbrains.compose.resources.StringResource

enum class SchoolLevel(
    val query: Int?,
    val stringResId: StringResource,
) {
    ALL(
        query = null,
        stringResId = Res.string.word_all,
    ),
    ONE(
        query = 1,
        stringResId = Res.string.all_timetable_cell_screen_grade_1,
    ),
    TWO(
        query = 2,
        stringResId = Res.string.all_timetable_cell_screen_grade_2,
    ),
    THREE(
        query = 3,
        stringResId = Res.string.all_timetable_cell_screen_grade_3,
    ),
    FOUR(
        query = 4,
        stringResId = Res.string.all_timetable_cell_screen_grade_4,
    ),
}
