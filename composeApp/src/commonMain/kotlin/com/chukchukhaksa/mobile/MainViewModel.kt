package com.chukchukhaksa.mobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chukchukhaksa.mobile.common.extension.record
import com.chukchukhaksa.mobile.common.kmp.getAppVersionName
import com.chukchukhaksa.mobile.common.kmp.getPlatform
import com.chukchukhaksa.mobile.common.model.NetworkException
import com.chukchukhaksa.mobile.common.model.UnknownException
import com.chukchukhaksa.mobile.common.ui.MviStore
import com.chukchukhaksa.mobile.common.ui.mviStore
import com.chukchukhaksa.mobile.domain.config.usecase.CheckNeedForceUpdateUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MainViewModel(
    val checkNeedForceUpdateUseCase: CheckNeedForceUpdateUseCase
) : ViewModel() {
    val mviStore: MviStore<MainState, MainSideEffect> = mviStore(MainState())

    private val mutex = Mutex()
    private var isFirstVisit: Boolean = true
    private var storeUrl: String? = null

    fun checkNeedForceUpdate() = viewModelScope.launch {
        if (isFirstVisit.not()) return@launch

        checkNeedForceUpdateUseCase(
            platform = getPlatform(),
            currentVersion = getAppVersionName(),
        ).onSuccess { (needForceUpdate, storeUrl) ->
            if (needForceUpdate.not()) return@onSuccess
            this@MainViewModel.storeUrl = storeUrl

            mviStore.setState { copy(showForceUpdateDialog = true) }
        }.onFailure {
            Napier.e("Error checking force update", it)
        }

        isFirstVisit = false
    }

    fun openAppStore() {
        storeUrl?.let {
            mviStore.postSideEffect(MainSideEffect.OpenUrl(it))
        }
    }

    fun onShowToast(msg: String) {
        viewModelScope.launch {
            mutex.withLock {
                mviStore.setState { copy(toastMessage = msg, toastVisible = true) }
                delay(SHOW_TOAST_LENGTH)
                mviStore.setState { copy(toastVisible = false) }
            }
        }
    }

    fun handleException(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> { mviStore.setState { copy(showNetworkErrorDialog = true) } }
//            is ConnectException -> { mviStore.setState { copy(showNetworkErrorDialog = true) } }
            else -> {
                onShowToast(throwable.message ?: UnknownException().message)
                throwable.record()
            }
        }
    }

    fun hideNetworkErrorDialog() = mviStore.setState { copy(showNetworkErrorDialog = false) }

    companion object {
        private const val SHOW_TOAST_LENGTH = 3000L
    }
}