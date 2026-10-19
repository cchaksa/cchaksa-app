package com.chukchukhaksa.mobile.presentation.portallink

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_school
import chukchukhaksa.composeapp.generated.resources.img_portal_link_error
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray600
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple100
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.Red300
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.presentation.portallink.component.CssEase
import com.chukchukhaksa.mobile.presentation.portallink.component.PortalLinkButton
import com.chukchukhaksa.mobile.presentation.portallink.component.PortalLinkConfirmDialog
import com.chukchukhaksa.mobile.presentation.portallink.component.PortalLinkLoadingAnimation
import com.chukchukhaksa.mobile.presentation.portallink.component.PortalLinkTextField
import com.chukchukhaksa.mobile.presentation.portallink.component.PortalLinkTopBar
import com.chukchukhaksa.mobile.presentation.portallink.component.keepAll
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkMode
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PortalLinkRoute(
  viewModel: PortalLinkViewModel = koinViewModel(),
  popBackStack: () -> Unit,
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

  viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
    when (sideEffect) {
      PortalLinkSideEffect.PopBackStack -> popBackStack()
    }
  }

  PortalLinkScreen(
    uiState = uiState,
    onStudentIdChange = viewModel::updateStudentId,
    onPasswordChange = viewModel::updatePassword,
    onClickSubmit = viewModel::submit,
    onClickUnavailable = viewModel::showUnavailableDialog,
    onDismissUnavailableDialog = viewModel::hideUnavailableDialog,
    onConfirmUnavailableDialog = viewModel::continueWithoutLink,
    onClickRetry = viewModel::retry,
    onClickBack = viewModel::popBackStack,
  )
}

/** 웹 /mpa/portal-login·/mpa/resync/login과 각 scraping 화면을 옮긴 화면. 배치와 문구는 웹을 따른다. */
@Composable
fun PortalLinkScreen(
  uiState: PortalLinkState,
  onStudentIdChange: (String) -> Unit = {},
  onPasswordChange: (String) -> Unit = {},
  onClickSubmit: () -> Unit = {},
  onClickUnavailable: () -> Unit = {},
  onDismissUnavailableDialog: () -> Unit = {},
  onConfirmUnavailableDialog: () -> Unit = {},
  onClickRetry: () -> Unit = {},
  onClickBack: () -> Unit = {},
) {
  SuwikiBackground(color = White100) {
    when (uiState.step) {
      PortalLinkStep.FORM -> PortalLinkForm(
        uiState = uiState,
        onStudentIdChange = onStudentIdChange,
        onPasswordChange = onPasswordChange,
        onClickSubmit = onClickSubmit,
        onClickUnavailable = onClickUnavailable,
        onClickBack = onClickBack,
      )

      PortalLinkStep.LOADING -> PortalLinkLoading()

      PortalLinkStep.FAILURE -> PortalLinkFailureContent(
        message = uiState.failureMessage,
        onClickRetry = onClickRetry,
      )
    }
  }

  if (uiState.isUnavailableDialogVisible) {
    PortalLinkConfirmDialog(
      message = "학교 연동없이 이용시\n'시간표 만들기'만 이용 가능합니다.",
      onConfirm = onConfirmUnavailableDialog,
      onDismiss = onDismissUnavailableDialog,
    )
  }
}

@Composable
private fun PortalLinkForm(
  uiState: PortalLinkState,
  onStudentIdChange: (String) -> Unit,
  onPasswordChange: (String) -> Unit,
  onClickSubmit: () -> Unit,
  onClickUnavailable: () -> Unit,
  onClickBack: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // 웹도 학업 정보 업데이트 화면에만 뒤로가기 상단 바가 있다.
    if (uiState.mode == PortalLinkMode.RESYNC) {
      PortalLinkTopBar(onClickBack = onClickBack)
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(top = 66.dp, bottom = 24.dp),
    ) {
      FunnelHeadline(
        title = "재학 중인 학교\n계정을 연동해주세요",
        description = "척척학사에서 수집하는 개인 정보는\n학교 연동 후 즉시 폐기됩니다.",
      )

      // 웹은 폼 컨테이너와 학교 카드에 각각 68px의 위쪽 여백을 둔다.
      Spacer(modifier = Modifier.height(136.dp))
      SchoolCard(schoolName = "수원대학교")

      Spacer(modifier = Modifier.height(12.dp))
      PortalLinkTextField(
        value = uiState.studentId,
        placeholder = "학번을 입력해주세요",
        isError = uiState.formErrorMessage != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
        onValueChange = onStudentIdChange,
      )

      Spacer(modifier = Modifier.height(12.dp))
      PortalLinkTextField(
        value = uiState.password,
        placeholder = "비밀번호를 입력해주세요",
        isError = uiState.formErrorMessage != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        visualTransformation = PasswordVisualTransformation(),
        onValueChange = onPasswordChange,
      )

      // 웹은 폼 간격 12px에 오류 문구의 위쪽 여백 12px을 더한다.
      uiState.formErrorMessage?.let { message ->
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          text = message.keepAll(),
          style = CchTheme.typography.bodyMd,
          color = Red300,
        )
      }

      // 웹은 폼 간격 12px에 링크의 위쪽 여백 16px, 안쪽 여백 8px을 둔다.
      if (uiState.mode == PortalLinkMode.LINK) {
        Spacer(modifier = Modifier.height(28.dp))
        Text(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .cchClickable(onClick = onClickUnavailable)
            .padding(8.dp),
          text = "즉시 학교 연동이 불가하신가요?\n(ex. 신입생, 편입생)".keepAll(),
          style = CchTheme.typography.bodySm.copy(fontWeight = FontWeight.Normal),
          color = Purple600,
          textAlign = TextAlign.Center,
          textDecoration = TextDecoration.Underline,
        )
      }
    }

    PortalLinkButton(
      modifier = Modifier
        .padding(start = 16.dp, end = 16.dp, top = 12.dp)
        // SuwikiBackground가 내비게이션 바만큼 이미 띄워 두었으므로 키보드 높이에서 그만큼 뺀다.
        .windowInsetsPadding(
          WindowInsets.ime
            .exclude(WindowInsets.navigationBars)
            .add(WindowInsets(bottom = KEYBOARD_BUTTON_MARGIN))
            .union(WindowInsets(bottom = BOTTOM_BUTTON_MARGIN)),
        ),
      text = "학업 이력 동기화하기",
      enabled = uiState.canSubmit,
      onClick = onClickSubmit,
    )
  }
}

@Composable
private fun FunnelHeadline(
  title: String,
  description: String,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = title.keepAll(),
      style = CchTheme.typography.titleLg,
      color = Black100,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = description.keepAll(),
      style = CchTheme.typography.bodyMd,
      color = Gray600,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun SchoolCard(schoolName: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(64.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(Purple100)
      .padding(horizontal = 18.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      modifier = Modifier.size(24.dp),
      painter = painterResource(Res.drawable.ic_school),
      contentDescription = null,
    )
    Spacer(modifier = Modifier.width(14.dp))
    Text(
      text = schoolName,
      style = CchTheme.typography.bodyLgStrong,
      color = Black100,
    )
  }
}

// 웹 FixedButton은 화면 아래에서 36px 위에 고정된다.
private val BOTTOM_BUTTON_MARGIN = 36.dp

// 키보드가 열리면 버튼을 키보드 바로 위에 붙인다. 웹은 키보드 위에도 36px을 남기지만 앱은 따르지 않는다.
private val KEYBOARD_BUTTON_MARGIN = 12.dp

private val LOADING_TITLES = listOf(
  "학교 정보를 불러오는 중이에요",
  "불러온 정보를 입력하고 있어요",
  "학교 인증이 거의 다 되었어요!",
)
private const val LOADING_DESCRIPTION = "척척학사에서 수집하는 개인 정보는\n학교 연동 후 즉시 폐기됩니다."
private const val TEXT_ROTATE_INTERVAL_MS = 2_500L
private const val FADE_DURATION_MS = 300

@Composable
private fun PortalLinkLoading() {
  var titleIndex by remember { mutableIntStateOf(0) }
  val textAlpha = remember { Animatable(1f) }

  // 웹 LoadingScreen은 2.5초 간격 타이머로 0.3초 동안 문구를 지우고, 문구를 바꾼 뒤 0.3초 동안 다시 보여 준다.
  LaunchedEffect(Unit) {
    var wait = TEXT_ROTATE_INTERVAL_MS
    while (true) {
      delay(wait)
      textAlpha.animateTo(0f, tween(FADE_DURATION_MS, easing = CssEase))
      titleIndex = (titleIndex + 1) % LOADING_TITLES.size
      textAlpha.animateTo(1f, tween(FADE_DURATION_MS, easing = CssEase))
      wait = TEXT_ROTATE_INTERVAL_MS - FADE_DURATION_MS * 2
    }
  }

  // 웹은 레이아웃 좌우 여백 20px 안에 로딩 화면의 좌우 여백 40px을 다시 둔다.
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(start = 60.dp, end = 60.dp, top = 141.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // 애니메이션은 300px 고정이라 좁은 화면에서는 여백을 넘어 가운데에 그려진다(웹과 같다).
    PortalLinkLoadingAnimation(modifier = Modifier.requiredSize(300.dp))
    Spacer(modifier = Modifier.height(40.dp))
    Column(
      // 투명도는 그리기 단계에서 읽어, 페이드하는 동안 화면 전체가 프레임마다 다시 구성되지 않게 한다.
      modifier = Modifier.graphicsLayer { alpha = textAlpha.value },
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = LOADING_TITLES[titleIndex].keepAll(),
        style = CchTheme.typography.titleLg,
        color = Black100,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(48.dp))
      Text(
        text = LOADING_DESCRIPTION.keepAll(),
        style = CchTheme.typography.bodyLg,
        color = Gray600,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun PortalLinkFailureContent(
  message: String,
  onClickRetry: () -> Unit,
) {
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    // 웹 오류 화면은 화면 높이의 16.6%만큼 위를 띄운다.
    val topPadding = maxHeight * 0.166f
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(start = 20.dp, end = 20.dp, top = topPadding, bottom = 64.dp + BOTTOM_BUTTON_MARGIN),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // 웹 ErrorScreen은 CSS 탓에 일러스트가 왼쪽으로 치우치고 문구의 줄바꿈(\n)이 공백으로 합쳐진다.
      // 의도한 모양이 아니라고 보고, 일러스트는 가운데에 두고 줄바꿈은 살린다. 문구 색은 웹과 같이 기본 검정이다.
      Image(
        modifier = Modifier.size(300.dp),
        painter = painterResource(Res.drawable.img_portal_link_error),
        contentDescription = null,
      )
      Spacer(modifier = Modifier.height(40.dp))
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = message.keepAll(),
        style = CchTheme.typography.titleLg,
        color = Color.Black,
        textAlign = TextAlign.Center,
      )
    }

    PortalLinkButton(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 16.dp, end = 16.dp, bottom = BOTTOM_BUTTON_MARGIN),
      text = "다시 시도하기",
      enabled = true,
      onClick = onClickRetry,
    )
  }
}

@Preview
@Composable
private fun PortalLinkFormPreview() {
  CchTheme {
    PortalLinkScreen(
      uiState = PortalLinkState(
        mode = PortalLinkMode.LINK,
        studentId = "20231234",
        formErrorMessage = "아이디나 비밀번호가 일치하지 않습니다.\n학교 홈페이지에서 확인해주세요.",
      ),
    )
  }
}

@Preview
@Composable
private fun PortalLinkFailurePreview() {
  CchTheme {
    PortalLinkScreen(
      uiState = PortalLinkState(
        mode = PortalLinkMode.RESYNC,
        step = PortalLinkStep.FAILURE,
        failureMessage = "알 수 없는 오류가 발생했어요\n잠시후 다시 시도해주세요",
      ),
    )
  }
}
