package com.chukchukhaksa.mobile.presentation.timetable.semesterselect

import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.TimetableEditorArgument
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class SemesterSelectState(
  val selectSemesterIndex: Int? = null,
  val selectSemester: Semester? = null,
  val nextButtonEnable: Boolean = false,
)

sealed interface SemesterSelectSideEffect {
  data class NavigateTimetableEditor(val semester: TimetableEditorArgument): SemesterSelectSideEffect
}

val semesterList: PersistentList<Semester> = run {
  val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
  val semesterList = mutableListOf<Semester>()
  for (year in currentYear downTo currentYear - 3) {
    semesterList
      .run {
        add(Semester(year.toString(), "1"))
        add(Semester(year.toString(), "2"))
      }
  }

  semesterList.toPersistentList()
}

data class Semester(
  val year: String,
  val semester: String,
) {
  fun toText() = "${year}년 ${semester}학기"
}

fun Semester.toTimetableEditorArgument() = TimetableEditorArgument(
  year = year,
  semester = semester,
)
