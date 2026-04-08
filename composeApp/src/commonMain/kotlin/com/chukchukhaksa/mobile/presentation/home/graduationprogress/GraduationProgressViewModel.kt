package com.chukchukhaksa.mobile.presentation.home.graduationprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.graduation.usecase.GetGraduationProgressUseCase
import kotlinx.coroutines.launch

class GraduationProgressViewModel(
    private val getGraduationProgressUseCase: GetGraduationProgressUseCase,
) : ViewModel() {
    val mviStore = mviStore<GraduationProgressState, GraduationProgressSideEffect>(GraduationProgressState())

    fun getGraduationProgress() = viewModelScope.launch {
        mviStore.setState { copy(isLoading = true) }
        getGraduationProgressUseCase()
            .onSuccess { graduationProgress ->
                mviStore.setState {
                    copy(
                        graduationProgress = graduationProgress,
                        isLoading = false,
                    )
                }
            }
            .onFailure {
                mviStore.setState { copy(isLoading = false) }
                mviStore.postSideEffect(GraduationProgressSideEffect.HandleException(it))
            }
    }
}
