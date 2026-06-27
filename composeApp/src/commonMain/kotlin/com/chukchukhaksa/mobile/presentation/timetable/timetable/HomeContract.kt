package com.chukchukhaksa.mobile.presentation.timetable.timetable

import com.chukchukhaksa.mobile.common.kmp.AdvertisingIdInfo
import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.academic.AcademicSummary
import com.chukchukhaksa.mobile.common.model.profile.Profile
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType

data class HomeState(
  val timetable: Timetable? = null,
  val cellType: TimetableCellType = TimetableCellType.CLASSNAME_PROFESSOR_LOCATION,
  val selectedCell: TimetableCell = TimetableCell(color = TimetableCellColor.GRAY_DARK),
  val showEditCellBottomSheet: Boolean = false,
  val selectedTab: HomeTab? = null,
  val showSelectCellTypeBottomSheet: Boolean = false,
  val profile: Profile? = null,
  val academicSummary: AcademicSummary? = null,
  val isPortalLinked: Boolean = false,
  val showPortalLinkDialog: Boolean = false,
  /** 디버그 전용: 시간표 탭 3연타로 조회한 IDFA 진단 결과. null이면 다이얼로그 숨김. */
  val idfaDebugInfo: AdvertisingIdInfo? = null,
)

sealed interface HomeSideEffect {
  data object ShowNeedCreateTimetableToast : HomeSideEffect
  data object NavigateAddTimetableCell : HomeSideEffect
  data object NavigateTimetableList : HomeSideEffect
  data class NavigateCellEditor(val argument: CellEditorArgument) : HomeSideEffect
  data class HandleException(val throwable: Throwable) : HomeSideEffect
  data object NavigateSemesterSelect : HomeSideEffect
}

enum class HomeTab {
  TIMETABLE,
  EMPTY_TIMETABLE,
  HOME
}
