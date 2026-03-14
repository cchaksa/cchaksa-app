package com.chukchukhaksa.mobile.presentation.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.img_landing_1
import chukchukhaksa.composeapp.generated.resources.img_landing_2
import chukchukhaksa.composeapp.generated.resources.img_landing_3
import chukchukhaksa.composeapp.generated.resources.img_landing_4
import chukchukhaksa.composeapp.generated.resources.img_landing_5
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.kmp.Platform
import com.chukchukhaksa.mobile.common.kmp.getPlatform
import com.chukchukhaksa.mobile.common.provider.LocalAppContext
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LandingRoute(
    viewModel: LandingViewModel = koinViewModel(),
    handleException: (Throwable) -> Unit,
    onShowToast: (String, Dp) -> Unit,
    navigateToHome: () -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
    val context = LocalAppContext.current

    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is LandingSideEffect.NavigateHome -> navigateToHome()
            is LandingSideEffect.ShowToast -> onShowToast(sideEffect.message, 70.dp)
            is LandingSideEffect.HandleException -> handleException(sideEffect.throwable)
        }
    }

    LandingScreen(
        uiState = uiState,
        onKakaoLogin = { viewModel.onKakaoLogin(context) },
        onAppleLogin = viewModel::onAppleLogin,
    )
}

@Composable
fun LandingScreen(
    uiState: LandingState = LandingState(),
    onKakaoLogin: () -> Unit = {},
    onAppleLogin: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            val imageModifier = Modifier.fillMaxWidth()

            Image(
                painter = painterResource(Res.drawable.img_landing_1),
                contentDescription = null,
                modifier = imageModifier,
                contentScale = ContentScale.FillWidth,
            )
            Image(
                painter = painterResource(Res.drawable.img_landing_2),
                contentDescription = null,
                modifier = imageModifier,
                contentScale = ContentScale.FillWidth,
            )
            Image(
                painter = painterResource(Res.drawable.img_landing_3),
                contentDescription = null,
                modifier = imageModifier,
                contentScale = ContentScale.FillWidth,
            )
            Image(
                painter = painterResource(Res.drawable.img_landing_4),
                contentDescription = null,
                modifier = imageModifier,
                contentScale = ContentScale.FillWidth,
            )
            Image(
                painter = painterResource(Res.drawable.img_landing_5),
                contentDescription = null,
                modifier = imageModifier,
                contentScale = ContentScale.FillWidth,
            )

            Spacer(modifier = Modifier.height(LOGIN_BUTTON_AREA_HEIGHT))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
        ) {
            CchBasicButton(
                modifier = Modifier.fillMaxWidth(),
                text = "카카오로 시작하기",
                enable = !uiState.isLoading,
                onClick = onKakaoLogin,
            )

            if (getPlatform() == Platform.IOS) {
                Spacer(modifier = Modifier.height(12.dp))

                CchBasicButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Apple로 시작하기",
                    enable = true,
                    onClick = onAppleLogin,
                )
            }
        }
    }
}

private val LOGIN_BUTTON_AREA_HEIGHT = 160.dp
