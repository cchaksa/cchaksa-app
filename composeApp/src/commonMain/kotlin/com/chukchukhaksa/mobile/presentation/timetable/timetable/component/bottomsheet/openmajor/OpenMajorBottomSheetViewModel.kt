package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.openmajor.usecase.GetOpenMajorListUseCase
import com.chukchukhaksa.mobile.domain.timetable.repository.OpenLectureRepository
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor.model.toOpenMajorList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class OpenMajorBottomSheetViewModel(
  private val getOpenMajorListUseCase: GetOpenMajorListUseCase,
  private val openLectureRepository: OpenLectureRepository,
) : ViewModel() {
  val mviStore: MviStore<OpenMajorState, OpenMajorSideEffect> = mviStore(OpenMajorState())

  private var selectedOpenMajor = "전체"
  private val allOpenMajorList = mutableListOf<String>()

  fun updateSearchValue(searchValue: String) {
    mviStore.setState { copy(searchValue = searchValue) }
    reduceOpenMajorList(searchValue)
  }

  fun updateSelectedOpenMajor(openMajor: String) {
    selectedOpenMajor = openMajor
    mviStore.postSideEffect(OpenMajorSideEffect.PopBackStackWithArgument(selectedOpenMajor))
  }

  fun popBackStack() {
    mviStore.postSideEffect(OpenMajorSideEffect.PopBackStack)
  }

  fun changeBottomShadowVisible(show: Boolean) {
    mviStore.setState { copy(showBottomShadow = show) }
  }

  fun setInitialSelectedOpenMajor(initialSelectedOpenMajor: String) {
    selectedOpenMajor = initialSelectedOpenMajor
  }

  fun initData() {
    mviStore.setState { copy(isLoading = true) }
    getOpenMajor()
    mviStore.setState { copy(isLoading = false) }
  }

  private fun getOpenMajor() {
    getOpenMajorListUseCase().onEach {
      allOpenMajorList.clear()
      val firebaseOpenMajor = openLectureRepository.getOpenMajor()
      allOpenMajorList.addAll((it + firebaseOpenMajor).distinct())
      reduceOpenMajorList()
    }.catch {
    }.launchIn(viewModelScope)
  }

  private fun reduceOpenMajorList(searchValue: String = mviStore.uiState.value.searchValue) {
    mviStore.setState {
      copy(
        filteredAllOpenMajorList = allOpenMajorList.toOpenMajorList(
          searchValue = searchValue,
          selectedOpenMajor = selectedOpenMajor,
        ),
      )
    }
  }
}
