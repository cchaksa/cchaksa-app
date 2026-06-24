package com.chukchukhaksa.mobile.presentation.timetable.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.kmp.AdvertisingIdProvider
import com.chukchukhaksa.mobile.common.kmp.isDebug
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
import com.chukchukhaksa.mobile.presentation.webview.HomeRedirectEvent
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
    private val showTimetableTabEventBus: ShowTimetableTabEventBus,
    private val advertisingIdProvider: AdvertisingIdProvider,
) : ViewModel() {
    val mviStore = mviStore<HomeState, HomeSideEffect>(HomeState())

    init {
        getProfile()
        getAcademicSummary()
        fetchPortalLinkStatus()
        refreshWebSession()
        observeHomeRedirect()
        observeShowTimetableTab()
    }

    /**
     * 시간표 생성·수정 완료 후, 홈 엔트리를 보존한 채 시간표 탭으로 전환하고 최신 시간표를 다시 불러온다.
     * (홈을 재생성하지 않으므로 홈 웹뷰 상태가 초기화되지 않는다.)
     */
    private fun observeShowTimetableTab() {
        viewModelScope.launch {
            showTimetableTabEventBus.events.collect {
                showTimetableTab()
            }
        }
    }

    private fun observeHomeRedirect() {
        // 랜딩 → 포털 웹뷰 → 홈 신규 진입처럼 구독 이전에 발생한 이벤트는 보류분에서 받아 처리한다.
        homeRedirectEventBus.consumePending()?.let { applyHomeRedirect(it) }
        viewModelScope.launch {
            homeRedirectEventBus.events.collect { event ->
                // 라이브로 처리했으므로 보류분을 비워 이후 신규 진입에서 재적용되지 않도록 한다.
                homeRedirectEventBus.consumePending()
                applyHomeRedirect(event)
            }
        }
    }

    private fun applyHomeRedirect(event: HomeRedirectEvent) {
        if (event.reloadWebView) {
            // 포털 연동 완료(done:portal-link) → 연동된 상태로 보고 홈 탭(웹뷰)을 보여준다.
            mviStore.setState { copy(isPortalLinked = true) }
            showHomeScreen()
        } else if (mviStore.uiState.value.isPortalLinked) {
            // 포털 연동 시 홈 탭, 미연동 시 시간표 탭을 선택한다.
            showHomeScreen()
        } else {
            showTimetableTab()
        }
    }

    private fun fetchPortalLinkStatus() {
        viewModelScope.launch {
            getPortalLinkStatusUseCase()
                .onSuccess { linked ->
                    mviStore.setState {
                        copy(
                            isPortalLinked = linked,
                            // 진입 탭 결정: 명시적으로 선택된 탭이 없으면
                            // 포털 연동 시 홈(웹뷰), 미연동 시 시간표 탭으로 진입한다.
                            selectedTab = selectedTab
                                ?: if (linked) HomeTab.HOME else timetableSideTab(timetable),
                        )
                    }
                }
                .onFailure {
                    // 연동 상태 확인 실패 시에도 빈 화면이 되지 않도록 시간표 탭으로 안전망 진입.
                    mviStore.setState {
                        copy(selectedTab = selectedTab ?: timetableSideTab(timetable))
                    }
                }
        }
    }

    private fun timetableSideTab(timetable: Timetable?): HomeTab =
        if (timetable == null) HomeTab.EMPTY_TIMETABLE else HomeTab.TIMETABLE

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
                        // 진입 탭 결정은 fetchPortalLinkStatus에 맡긴다.
                        // 여기서는 이미 시간표 쪽 탭일 때만 데이터에 맞춰 EMPTY↔TIMETABLE을 보정하고,
                        // 홈 탭이나 아직 미결정(null) 상태는 건드리지 않는다.
                        selectedTab = if (selectedTab == HomeTab.TIMETABLE || selectedTab == HomeTab.EMPTY_TIMETABLE) {
                            timetableSideTab(timetable)
                        } else {
                            selectedTab
                        },
                    )
                }
            }
            .onFailure {
                mviStore.postSideEffect(HomeSideEffect.HandleException(it))
            }
    }

    fun showTimetableTab() {
        mviStore.setState { copy(selectedTab = timetableSideTab(timetable)) }
        getMainTimetable()
        if (isDebug) copyAdvertisingIdToClipboard()
    }

    /**
     * 디버그 빌드에서 시간표 탭을 누를 때마다 IDFA를 클립보드에 복사하도록
     * [HomeSideEffect.CopyIdfaToClipboard]를 발행한다(AdMob 테스트 기기 등록용).
     * iOS만 IDFA가 조회되고 Android는 null이라 자연히 무동작이다.
     */
    private fun copyAdvertisingIdToClipboard() {
        advertisingIdProvider.getAdvertisingId()?.let { idfa ->
            mviStore.postSideEffect(HomeSideEffect.CopyIdfaToClipboard(idfa))
        }
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
