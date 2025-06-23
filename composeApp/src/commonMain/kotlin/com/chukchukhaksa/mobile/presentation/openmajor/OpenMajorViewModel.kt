package com.chukchukhaksa.mobile.presentation.openmajor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.openmajor.usecase.GetOpenMajorListUseCase
import com.chukchukhaksa.mobile.domain.timetable.repository.OpenLectureRepository
import com.chukchukhaksa.mobile.presentation.openmajor.model.toOpenMajorList
import com.chukchukhaksa.mobile.presentation.openmajor.navigation.OpenMajorRoute
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class OpenMajorViewModel(
    private val getOpenMajorListUseCase: GetOpenMajorListUseCase,
    private val openLectureRepository: OpenLectureRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val mviStore: MviStore<OpenMajorState, OpenMajorSideEffect> = mviStore(OpenMajorState())

    private var selectedOpenMajor = savedStateHandle[OpenMajorRoute.ARGUMENT_NAME] ?: "전체"
    private val allOpenMajorList = mutableListOf<String>()

    fun updateSearchValue(searchValue: String) {
        mviStore.setState { copy(searchValue = searchValue) }
        reduceOpenMajorList(searchValue)
    }

    fun syncPagerState(currentPage: Int) {
        mviStore.setState { copy(currentPage = currentPage) }
    }

    fun updateSelectedOpenMajor(openMajor: String) {
        selectedOpenMajor = openMajor
        reduceOpenMajorList()
    }

    fun popBackStack() {
        mviStore.postSideEffect(OpenMajorSideEffect.PopBackStack)
    }

    fun popBackStackWithArgument() {
        mviStore.postSideEffect(OpenMajorSideEffect.PopBackStackWithArgument(selectedOpenMajor))
    }

    fun changeBottomShadowVisible(show: Boolean) {
        mviStore.setState { copy(showBottomShadow = show) }
    }

    fun initData() {
        mviStore.setState { copy(isLoading = true) }
        getOpenMajor()
        mviStore.setState{ copy(isLoading = false) }
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
