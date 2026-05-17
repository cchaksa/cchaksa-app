package com.chukchukhaksa.mobile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
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
import com.chukchukhaksa.mobile.common.kmp.Platform.*
import com.chukchukhaksa.mobile.common.kmp.getPlatform
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.domain.auth.usecase.CheckAuthStateUseCase
import com.chukchukhaksa.mobile.domain.webview.WebViewPreloader
import com.chukchukhaksa.mobile.presentation.landing.navigation.LandingRoute
import com.chukchukhaksa.mobile.presentation.landing.navigation.landingNavGraph
import com.chukchukhaksa.mobile.presentation.timetable.navigation.TimetableRoute
import com.chukchukhaksa.mobile.presentation.timetable.navigation.timetableNavGraph
import com.chukchukhaksa.mobile.presentation.webview.navigation.webViewNavGraph
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.analytics
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    navigator: MainNavigator = rememberMainNavigator(),
    onReady: () -> Unit = {},
) {
    CchTheme {
        KoinContext {
            val uiState = viewModel.mviStore.uiState.collectAsState().value
            val uriHandler = LocalUriHandler.current
            val checkAuthStateUseCase: CheckAuthStateUseCase = koinInject()
            val webViewPreloader: WebViewPreloader = koinInject()
            var startDestination by remember { mutableStateOf<String?>(null) }

            viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
                when (sideEffect) {
                    is MainSideEffect.OpenUrl -> uriHandler.openUri(sideEffect.url)
                }
            }

            LaunchedEffect(Unit) {
                val isAuthenticated = checkAuthStateUseCase().getOrDefault(false)
                startDestination = if (isAuthenticated) TimetableRoute.route else LandingRoute.route
//              startDestination = TimetableRoute.route
                if (isAuthenticated) {
                    webViewPreloader.preload()
                }
                onReady()
            }

            LaunchedEffect(key1 = Unit) {
                viewModel.checkNeedForceUpdate()

                delay(1000)
              val name = when (getPlatform()) {
                Android -> "android_startup"
                IOS -> "ios_startup"
              }
                Firebase.analytics.logEvent(
                    name = name,
                    parameters = mapOf(
                        "platform" to getPlatform().name
                    )
                )
            }

            Scaffold(
                containerColor = White,
                contentWindowInsets = WindowInsets(0.dp),
                modifier = modifier,
                content = { innerPadding ->
                    val currentStartDestination = startDestination

                    if (currentStartDestination != null) {
                        val navGraphBuilder: NavGraphBuilder.() -> Unit = {
                            landingNavGraph(
                                handleException = viewModel::handleException,
                                onShowToast = viewModel::onShowToast,
                                navigateToHome = {
                                    webViewPreloader.preload()
                                    navigator.navigateFromLandingToHome()
                                },
                            )

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
                                navigateWebView = navigator::navigateWebView,
                                navigateToLanding = navigator::navigateToLanding,
                            )

                            webViewNavGraph(
                                popBackStack = navigator::popBackStackIfNotHome,
                                onNavigateWebView = navigator::navigateWebView,
                            )
                        }

                        when (getPlatform()) {
                            Android -> NavHost(
                                navController = navigator.navController,
                                startDestination = currentStartDestination,
                                enterTransition = { slideInHorizontally(tween(350, easing = FastOutSlowInEasing)) { it } },
                                exitTransition = { slideOutHorizontally(tween(350, easing = FastOutSlowInEasing)) { -it / 3 } },
                                popEnterTransition = { slideInHorizontally(tween(350, easing = FastOutSlowInEasing)) { -it / 3 } },
                                popExitTransition = { slideOutHorizontally(tween(350, easing = FastOutSlowInEasing)) { it } },
                                builder = navGraphBuilder,
                            )

                            IOS -> NavHost(
                                navController = navigator.navController,
                                startDestination = currentStartDestination,
                                builder = navGraphBuilder,
                            )
                        }
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
