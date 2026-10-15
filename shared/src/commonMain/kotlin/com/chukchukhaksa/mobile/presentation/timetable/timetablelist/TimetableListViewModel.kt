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
        if (toDeleteTimetable == null) return@launch

        val state = mviStore.uiState.value

        runCatchingIgnoreCancelled { timetableRepository.deleteTimetable(toDeleteTimetable!!) }
            .onSuccess {
                mviStore.setState {
                    copy(
                        timetableList = state.timetableList.minus(toDeleteTimetable!!)
                            .toPersistentList()
                    )
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
