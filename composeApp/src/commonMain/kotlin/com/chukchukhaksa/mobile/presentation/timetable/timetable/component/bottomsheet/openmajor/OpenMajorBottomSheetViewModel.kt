package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.timetable.repository.OpenLectureRepository
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor.model.toOpenMajorList
import kotlinx.coroutines.launch

class OpenMajorBottomSheetViewModel(
  private val openLectureRepository: OpenLectureRepository,
) : ViewModel() {
  val mviStore: MviStore<OpenMajorState, OpenMajorSideEffect> = mviStore(OpenMajorState())

  private var selectedOpenMajor: String? = null
  private val allOpenMajorList = mutableListOf<String?>()

  fun updateSearchValue(searchValue: String) {
    mviStore.setState { copy(searchValue = searchValue) }
    reduceOpenMajorList(searchValue)
  }

  fun updateSelectedOpenMajor(openMajor: String?) {
    selectedOpenMajor = openMajor
    mviStore.postSideEffect(OpenMajorSideEffect.PopBackStackWithArgument(selectedOpenMajor))
  }

  fun popBackStack() {
    mviStore.postSideEffect(OpenMajorSideEffect.PopBackStack)
  }

  fun changeBottomShadowVisible(show: Boolean) {
    mviStore.setState { copy(showBottomShadow = show) }
  }

  fun setInitialSelectedOpenMajor(initialSelectedOpenMajor: String?) {
    selectedOpenMajor = initialSelectedOpenMajor
  }

  fun initData() = viewModelScope.launch {
    mviStore.setState { copy(isLoading = true) }

    allOpenMajorList.clear()
    val firebaseOpenMajor = openLectureRepository.getOpenMajor()
    allOpenMajorList.addAll((listOf(null) + firebaseOpenMajor).distinct())
    reduceOpenMajorList()

    mviStore.setState { copy(isLoading = false) }
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
