package com.chukchukhaksa.mobile

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.dialog_network_body
import chukchukhaksa.composeapp.generated.resources.dialog_network_header
import chukchukhaksa.composeapp.generated.resources.dialog_update_mandatory_body
import chukchukhaksa.composeapp.generated.resources.dialog_update_mandatory_header
import chukchukhaksa.composeapp.generated.resources.word_confirm
import com.chukchukhaksa.mobile.common.designsystem.component.dialog.CchDialog
import com.chukchukhaksa.mobile.common.designsystem.component.toast.CchToast
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.kmp.SetStatusBarColor
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.presentation.timetable.navigation.timetableNavGraph
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    navigator: MainNavigator = rememberMainNavigator(),
) {
    SetStatusBarColor()
    CchTheme {
        KoinContext {
            val uiState = viewModel.mviStore.uiState.collectAsState().value
            val uriHandler = LocalUriHandler.current
            viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
                when (sideEffect) {
                    is MainSideEffect.OpenUrl -> uriHandler.openUri(sideEffect.url)
                }
            }

            LaunchedEffect(key1 = Unit) {
                viewModel.checkNeedForceUpdate()
            }

            Scaffold(
                containerColor = White,
                contentWindowInsets = WindowInsets(0.dp),
                modifier = modifier,
                content = { innerPadding ->
                    NavHost(
                        navController = navigator.navController,
                        startDestination = navigator.startDestination,
                    ) {

                        timetableNavGraph(
                            padding = innerPadding,
                            popBackStack = navigator::popBackStackIfNotHome,
                            navigateTimetableNameInput = navigator::navigateTimetableNameInput,
                            navigateTimetableEditor = navigator::navigateTimetableEditor,
                            navigateTimetableList = navigator::navigateTimetableList,
                            navigateOpenLecture = navigator::navigateOpenLecture,
                            handleException = viewModel::handleException,
                            onShowToast = viewModel::onShowToast,
                            navigateCellEditor = navigator::navigateCellEditor,
                            navigateSemesterSelect = navigator::navigateSemesterSelect,
                            navigateTimetable = navigator::navigateTimetable,
                        )
                    }

                    if (uiState.showNetworkErrorDialog) {
                        CchDialog(
                            headerText = stringResource(Res.string.dialog_network_header),
                            bodyText = stringResource(Res.string.dialog_network_body),
                            confirmButtonText = stringResource(Res.string.word_confirm),
                            onDismissRequest = viewModel::hideNetworkErrorDialog,
                            onClickConfirm = viewModel::hideNetworkErrorDialog,
                        )
                    }

                    if (uiState.showForceUpdateDialog) {
                        CchDialog(
                            headerText = stringResource(Res.string.dialog_update_mandatory_header),
                            bodyText = stringResource(Res.string.dialog_update_mandatory_body),
                            confirmButtonText = stringResource(Res.string.word_confirm),
                            onDismissRequest = {},
                            onClickConfirm = viewModel::openAppStore,
                        )
                    }

                    CchToast(
                        visible = uiState.toastVisible,
                        message = uiState.toastMessage,
                        bottomPadding = uiState.toastBottomPadding + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    )
                },
            )
        }
    }
}
