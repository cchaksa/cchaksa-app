package com.chukchukhaksa.mobile.presentation.timetable.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.academic.usecase.GetAcademicSummaryUseCase
import com.chukchukhaksa.mobile.domain.profile.usecase.GetProfileUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.DeleteTimetableCellUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetMainTimetableUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetTimetableCellTypeUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.SetTimetableCellTypeUseCase
import com.chukchukhaksa.mobile.domain.user.usecase.GetPortalLinkStatusUseCase
import com.chukchukhaksa.mobile.domain.webview.ExchangeWebSessionUseCase
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.toCellEditorArgument
import com.chukchukhaksa.mobile.presentation.webview.HomeRedirectEventBus
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getMainTimetableUseCase: GetMainTimetableUseCase,
    private val getTimetableCellTypeUseCase: GetTimetableCellTypeUseCase,
    private val deleteTimetableCellUseCase: DeleteTimetableCellUseCase,
    private val setTimetableCellTypeUseCase: SetTimetableCellTypeUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getAcademicSummaryUseCase: GetAcademicSummaryUseCase,
    private val getPortalLinkStatusUseCase: GetPortalLinkStatusUseCase,
    private val exchangeWebSessionUseCase: ExchangeWebSessionUseCase,
    private val homeRedirectEventBus: HomeRedirectEventBus,
) : ViewModel() {
    val mviStore = mviStore<HomeState, HomeSideEffect>(HomeState())

    init {
        getProfile()
        getAcademicSummary()
        fetchPortalLinkStatus()
        refreshWebSession()
        observeHomeRedirect()
    }

    private fun observeHomeRedirect() {
        viewModelScope.launch {
            homeRedirectEventBus.events.collect {
                // 포털 연동 시 홈 탭, 미연동 시 시간표 탭을 선택한다.
                if (mviStore.uiState.value.isPortalLinked) {
                    showHomeScreen()
                } else {
                    showTimetableTab()
                }
            }
        }
    }

    private fun fetchPortalLinkStatus() {
        viewModelScope.launch {
            getPortalLinkStatusUseCase()
                .onSuccess { linked ->
                    mviStore.setState { copy(isPortalLinked = linked) }
                }
        }
    }

    private fun refreshWebSession() {
        viewModelScope.launch {
            exchangeWebSessionUseCase.refresh()
        }
    }

    fun getMainTimetable() = viewModelScope.launch {
        val cellType = TimetableCellType.getType(getTimetableCellTypeUseCase().getOrNull())

        getMainTimetableUseCase()
            .onSuccess { timetable ->
                mviStore.setState {
                    copy(
                        timetable = timetable,
                        cellType = cellType,
                        selectedTab = selectedTab
                            ?: if (timetable == null) HomeTab.EMPTY_TIMETABLE else HomeTab.TIMETABLE,
                    )
                }
            }
            .onFailure {
                mviStore.postSideEffect(HomeSideEffect.HandleException(it))
            }
    }

    fun showTimetableTab() {
        mviStore.setState {
            copy(
                selectedTab = if (timetable == null) HomeTab.EMPTY_TIMETABLE else HomeTab.TIMETABLE,
            )
        }
        getMainTimetable()
    }

    fun deleteCell(cell: TimetableCell) = viewModelScope.launch {
        deleteTimetableCellUseCase(cell)
            .onSuccess {
                mviStore.setState {
                    copy(
                        showEditCellBottomSheet = false,
                        timetable = it,
                    )
                }
            }
            .onFailure { mviStore.postSideEffect(HomeSideEffect.HandleException(it)) }
    }

    fun showEditCellBottomSheet(cell: TimetableCell) = viewModelScope.launch {
        mviStore.setState {
            copy(
                showEditCellBottomSheet = true,
                selectedCell = cell,
            )
        }
    }

    fun updateCellType(position: Int) = viewModelScope.launch {
        val cellType = TimetableCellType.entries[position]
        setTimetableCellTypeUseCase(cellType.name)
            .onSuccess { mviStore.setState { copy(cellType = cellType) } }
    }

    fun getProfile() {
      viewModelScope.launch {
        getProfileUseCase()
          .onSuccess { profile ->
            mviStore.setState { copy(profile = profile) }
          }
      }
    }

    fun getAcademicSummary() {
      viewModelScope.launch {
        getAcademicSummaryUseCase()
          .onSuccess { academicSummary ->
            mviStore.setState { copy(academicSummary = academicSummary) }
          }
      }
    }

    fun showSelectCellTypeBottomSheet() {
        mviStore.setState { copy(showSelectCellTypeBottomSheet = true) }
    }

    fun hideSelectCellTypeBottomSheet() {
        mviStore.setState { copy(showSelectCellTypeBottomSheet = false) }
    }

    fun navigateCellEdit(cell: TimetableCell) {
        mviStore.postSideEffect(HomeSideEffect.NavigateCellEditor(cell.toCellEditorArgument()))
    }

    fun hideEditCellBottomSheet() {
        mviStore.setState { copy(showEditCellBottomSheet = false) }
    }

    fun showHomeScreen() {
        mviStore.setState { copy(selectedTab = HomeTab.HOME) }
    }

    fun showPortalLinkDialog() {
        mviStore.setState { copy(showPortalLinkDialog = true) }
    }

    fun hidePortalLinkDialog() {
        mviStore.setState { copy(showPortalLinkDialog = false) }
    }

    fun navigateSemesterSelect() {
      mviStore.postSideEffect(HomeSideEffect.NavigateSemesterSelect)
    }


  fun navigateTimetableList() {
        mviStore.postSideEffect(HomeSideEffect.NavigateTimetableList)
    }

    fun navigateAddTimetableCell() {
        val state = mviStore.uiState.value
        if (state.timetable == null) {
            mviStore.postSideEffect(HomeSideEffect.ShowNeedCreateTimetableToast)
        } else {
            mviStore.postSideEffect(HomeSideEffect.NavigateAddTimetableCell)
        }
    }
}
