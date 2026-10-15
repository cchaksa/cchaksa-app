package com.chukchukhaksa.mobile.presentation.timetable.timetablelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.extension.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.data.timetable.repository.TimetableRepository
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.toTimetableEditorArgument
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

class TimetableListViewModel(
    private val timetableRepository: TimetableRepository,
) : ViewModel() {
    val mviStore: MviStore<TimetableListState, TimetableListSideEffect> =
        mviStore(TimetableListState())
    private var toDeleteTimetable: Timetable? = null

    fun initData() = viewModelScope.launch {
        runCatchingIgnoreCancelled { timetableRepository.getAllTimetable() }
            .onSuccess {
                mviStore.setState { copy(timetableList = it.toPersistentList()) }
            }
            .onFailure {
                mviStore.postSideEffect(TimetableListSideEffect.HandleException(it))
            }
    }

    fun deleteTimetable() = viewModelScope.launch {
        // 삭제가 끝날 때까지 다이얼로그가 떠 있으므로, 확인을 연달아 누르면 두 번째 호출이 들어온다.
        // 대상을 지역 변수로 잡고 필드를 바로 비워 두 번째 호출은 아무 일도 하지 않게 한다.
        val target = toDeleteTimetable ?: return@launch
        toDeleteTimetable = null

        runCatchingIgnoreCancelled { timetableRepository.deleteTimetable(target) }
            .onSuccess {
                mviStore.setState {
                    copy(timetableList = timetableList.remove(target))
                }
                mviStore.postSideEffect(TimetableListSideEffect.ShowNeedTimetableDeleteToast)
            }
            .onFailure {
                mviStore.postSideEffect(TimetableListSideEffect.HandleException(it))
            }
        hideDeleteDialog()
    }

    fun popBackStack() {
        mviStore.postSideEffect(TimetableListSideEffect.PopBackStack)
    }

    fun navigateTimetableEditor(timetable: Timetable) {
      mviStore.postSideEffect(TimetableListSideEffect.NavigateTimetableEditor(timetable.toTimetableEditorArgument()))
    }

    fun navigateSemesterSelect() {
      mviStore.postSideEffect(TimetableListSideEffect.NavigateSemesterSelect)
    }

    fun showDeleteDialog(timetable: Timetable) {
        toDeleteTimetable = timetable
        mviStore.setState { copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        toDeleteTimetable = null
        mviStore.setState { copy(showDeleteDialog = false) }
    }

    fun setMainTimetable(createTime: Long) = viewModelScope.launch {
        runCatchingIgnoreCancelled { timetableRepository.setMainTimetableCreateTime(createTime) }
            .onSuccess { popBackStack() }
            .onFailure { mviStore.postSideEffect(TimetableListSideEffect.HandleException(it)) }
    }
}
