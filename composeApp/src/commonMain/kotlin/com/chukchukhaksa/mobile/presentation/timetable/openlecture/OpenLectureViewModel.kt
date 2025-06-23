package com.chukchukhaksa.mobile.presentation.timetable.openlecture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.model.OpenLecture
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableCellOverlapException
import com.chukchukhaksa.mobile.common.model.TimetableDay
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.timetable.repository.OpenLectureRepository
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetOpenLectureListUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.InsertTimetableCellUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.UpdateOpenLectureIfNeedUseCase
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.toCellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.openlecture.model.SchoolLevel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OpenLectureViewModel(
    private val updateOpenLectureIfNeedUseCase: UpdateOpenLectureIfNeedUseCase,
    private val getOpenLectureListUseCase: GetOpenLectureListUseCase,
    private val insertTimetableCellUseCase: InsertTimetableCellUseCase,
    private val openLectureRepository: OpenLectureRepository,
) : ViewModel() {

    private val mutex: Mutex = Mutex()

    val mviStore: MviStore<OpenLectureState, OpenLectureSideEffect> = mviStore(OpenLectureState())

    private val currentState
        get() = mviStore.uiState.value

    private var searchQuery: String = ""
    private var isFirstVisit: Boolean = true

    private var selectedOpenLecture: OpenLecture? = null

    fun initData() = viewModelScope.launch {
        if (isFirstVisit) {
            mviStore.setState { copy(isLoading = true) }
            updateOpenLectureIfNeedUseCase()
            val lastUpdated = openLectureRepository.getLastUpdatedDate()
            mviStore.setState {
                copy(
                    lastUpdatedDate = lastUpdated,
                )
            }
            getOpenLectureList()
            isFirstVisit = false
        }
    }

    fun navigateCellEditor(openLecture: OpenLecture = OpenLecture()) {
        mviStore.postSideEffect(
            OpenLectureSideEffect.NavigateCellEditor(openLecture.toCellEditorArgument()),
        )
    }

    fun navigateAddCustomCell() {
        mviStore.postSideEffect(OpenLectureSideEffect.NavigateAddCustomTimetableCell)
    }

    fun insertTimetable() = viewModelScope.launch {
        if (selectedOpenLecture == null) return@launch
        val state = currentState

        val timetableCellList = if (selectedOpenLecture!!.originalCellList.isEmpty()) {
            listOf(
                TimetableCell(
                    name = selectedOpenLecture!!.name,
                    professor = selectedOpenLecture!!.professorName,
                    location = "",
                    day = TimetableDay.E_LEARNING,
                    startPeriod = 0,
                    endPeriod = 0,
                    color = state.selectedTimetableCellColor,
                ),
            )
        } else {
            selectedOpenLecture!!.originalCellList.map { cell ->
                TimetableCell(
                    name = selectedOpenLecture!!.name,
                    professor = selectedOpenLecture!!.professorName,
                    location = cell.location,
                    day = cell.day,
                    startPeriod = cell.startPeriod,
                    endPeriod = cell.endPeriod,
                    color = state.selectedTimetableCellColor,
                )
            }
        }

        insertTimetableCellUseCase(timetableCellList)
            .onSuccess {
                mviStore.postSideEffect(OpenLectureSideEffect.ShowSuccessAddCellToast)
            }
            .onFailure {
                if (it is TimetableCellOverlapException) {
                    mviStore.postSideEffect(OpenLectureSideEffect.ShowOverlapCellToast(it.message))
                } else {
                    mviStore.postSideEffect(OpenLectureSideEffect.HandleException(it))
                }
            }
    }

    fun updateSelectedCellColor(color: TimetableCellColor) {
        mviStore.setState { copy(selectedTimetableCellColor = color) }
    }

    fun showSelectColorBottomSheet(openLecture: OpenLecture) {
        mviStore.setState {
            selectedOpenLecture = openLecture
            copy(
                selectedTimetableCellColor = TimetableCellColor.entries.shuffled().first(),
                showSelectCellColorBottomSheet = true,
            )
        }
    }

    fun hideSelectColorBottomSheet() {
        mviStore.setState { copy(showSelectCellColorBottomSheet = false) }
    }

    fun searchOpenLecture(search: String) {
        searchQuery = search
        getOpenLectureList(search = search)
    }

    fun updateSearchValue(searchValue: String) {
        mviStore.setState { copy(searchValue = searchValue) }
    }

    fun updateSchoolLevelPosition(schoolLevel: SchoolLevel) {
        mviStore.setState {
            copy(
                schoolLevel = schoolLevel,
            )
        }

        getOpenLectureList()
    }

    fun updateSelectedOpenMajor(openMajor: String) {
        if (openMajor == mviStore.uiState.value.selectedOpenMajor) return

        mviStore.setState {
            copy(
                selectedOpenMajor = openMajor,
            )
        }
        getOpenLectureList()
    }

    private fun getOpenLectureList(
        search: String = searchQuery,
    ) = viewModelScope.launch {
        mutex.withLock {
            getOpenLectureListUseCase(
                GetOpenLectureListUseCase.Param(
                    lectureOrProfessorName = search,
                    major = if (currentState.selectedOpenMajor == "전체") null else currentState.selectedOpenMajor,
                    grade = currentState.schoolLevel.query,
                ),
            ).onSuccess { newData ->
                mviStore.setState {
                    copy(
                        isLoading = false,
                        openLectureList = newData
                            .distinctBy { it.id }
                            .toPersistentList(),
                    )
                }
                mviStore.postSideEffect(OpenLectureSideEffect.ScrollToTop)
            }.onFailure {
                mviStore.postSideEffect(OpenLectureSideEffect.HandleException(it))
            }
        }
    }

    fun showGradeBottomSheet() {
        mviStore.setState { copy(showSchoolLevelBottomSheet = true) }
    }

    fun hideGradeBottomSheet() {
        mviStore.setState { copy(showSchoolLevelBottomSheet = false) }
    }

    fun popBackStack() {
        mviStore.postSideEffect(OpenLectureSideEffect.PopBackStack)
    }
}
