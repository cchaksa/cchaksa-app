package com.chukchukhaksa.mobile.presentation.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_apple_logo
import chukchukhaksa.composeapp.generated.resources.ic_kakao_logo
import chukchukhaksa.composeapp.generated.resources.img_landing_1
import chukchukhaksa.composeapp.generated.resources.img_landing_2
import chukchukhaksa.composeapp.generated.resources.img_landing_3
import chukchukhaksa.composeapp.generated.resources.img_landing_4
import chukchukhaksa.composeapp.generated.resources.img_landing_5
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.kmp.Platform
import com.chukchukhaksa.mobile.common.kmp.getPlatform
import com.chukchukhaksa.mobile.common.provider.LocalAppContext
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LandingRoute(
    viewModel: LandingViewModel = koinViewModel(),
    handleException: (Throwable) -> Unit,
    onShowToast: (String, Dp) -> Unit,
    navigateToHome: (isPortalLinked: Boolean) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
    val context = LocalAppContext.current

    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is LandingSideEffect.NavigateHome -> navigateToHome(sideEffect.isPortalLinked)
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

            listOf(
                Res.drawable.img_landing_1,
                Res.drawable.img_landing_2,
                Res.drawable.img_landing_3,
                Res.drawable.img_landing_4,
                Res.drawable.img_landing_5,
            ).forEach { imageRes ->
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = imageModifier,
                    contentScale = ContentScale.FillWidth,
                )
            }

            Spacer(modifier = Modifier.height(LOGIN_BUTTON_AREA_HEIGHT))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
        ) {
            SocialLoginButton(
                modifier = Modifier.fillMaxWidth(),
                text = "3초만에 카카오톡으로 시작하기",
                iconRes = Res.drawable.ic_kakao_logo,
                containerColor = KakaoYellow,
                contentColor = KakaoBlack,
                enabled = !uiState.isLoading,
                loading = uiState.loadingProvider == LoginProvider.KAKAO,
                onClick = onKakaoLogin,
            )

            if (getPlatform() == Platform.IOS) {
                Spacer(modifier = Modifier.height(12.dp))

                SocialLoginButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "애플 아이디로 시작하기",
                    iconRes = Res.drawable.ic_apple_logo,
                    containerColor = Black100,
                    contentColor = White100,
                    enabled = !uiState.isLoading,
                    loading = uiState.loadingProvider == LoginProvider.APPLE,
                    onClick = onAppleLogin,
                )
            }
        }
    }
}

private val LOGIN_BUTTON_AREA_HEIGHT = 160.dp

private val KakaoYellow = Color(0xFFFEE500)
private val KakaoBlack = Color(0xD9000000) // 85% opacity

@Composable
private fun SocialLoginButton(
    modifier: Modifier = Modifier,
    text: String,
    iconRes: DrawableResource,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val clickableModifier = if (enabled) {
        Modifier.cchClickable { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .wrapContentHeight()
            .clip(RoundedCornerShape(10.dp))
            .then(clickableModifier)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            // 로딩 중에도 버튼 높이가 흔들리지 않도록 아이콘과 동일한 32dp 높이를 유지한다.
            if (loading) {
                Box(modifier = Modifier.height(32.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text,
                        color = contentColor,
                        style = CchTheme.typography.bodyMdStrong,
                    )
                }
            }
        }

        // 로그인 진행 중 비활성화된 버튼은 반투명 흰색을 덮어 클릭 불가 상태를 표시한다.
        if (!enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(White100.copy(alpha = 0.6f)),
            )
        }

        // 로딩 인디케이터는 반투명 오버레이 위에 선명하게 보이도록 가장 마지막에 그린다.
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp),
                color = contentColor,
                strokeWidth = 3.dp,
            )
        }
    }
}

@Composable
private fun SocialLoginButtonPreviewContainer(content: @Composable () -> Unit) {
    CchTheme {
        Box(
            modifier = Modifier
                .background(White100)
                .width(360.dp)
                .padding(16.dp),
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun SocialLoginButtonKakaoPreview() {
    SocialLoginButtonPreviewContainer {
        SocialLoginButton(
            modifier = Modifier.fillMaxWidth(),
            text = "3초만에 카카오톡으로 시작하기",
            iconRes = Res.drawable.ic_kakao_logo,
            containerColor = KakaoYellow,
            contentColor = KakaoBlack,
            enabled = true,
            loading = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SocialLoginButtonKakaoLoadingPreview() {
    SocialLoginButtonPreviewContainer {
        SocialLoginButton(
            modifier = Modifier.fillMaxWidth(),
            text = "3초만에 카카오톡으로 시작하기",
            iconRes = Res.drawable.ic_kakao_logo,
            containerColor = KakaoYellow,
            contentColor = KakaoBlack,
            enabled = false,
            loading = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SocialLoginButtonKakaoDisabledPreview() {
    SocialLoginButtonPreviewContainer {
        SocialLoginButton(
            modifier = Modifier.fillMaxWidth(),
            text = "3초만에 카카오톡으로 시작하기",
            iconRes = Res.drawable.ic_kakao_logo,
            containerColor = KakaoYellow,
            contentColor = KakaoBlack,
            enabled = false,
            loading = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SocialLoginButtonApplePreview() {
    SocialLoginButtonPreviewContainer {
        SocialLoginButton(
            modifier = Modifier.fillMaxWidth(),
            text = "애플 아이디로 시작하기",
            iconRes = Res.drawable.ic_apple_logo,
            containerColor = Black100,
            contentColor = White100,
            enabled = true,
            loading = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SocialLoginButtonAppleLoadingPreview() {
    SocialLoginButtonPreviewContainer {
        SocialLoginButton(
            modifier = Modifier.fillMaxWidth(),
            text = "애플 아이디로 시작하기",
            iconRes = Res.drawable.ic_apple_logo,
            containerColor = Black100,
            contentColor = White100,
            enabled = false,
            loading = true,
            onClick = {},
        )
    }
}
