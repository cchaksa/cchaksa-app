package com.chukchukhaksa.mobile.presentation.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.CchAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray300
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray600
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple100
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkStrategy
import com.chukchukhaksa.mobile.presentation.portallink.navigation.PortalLinkMode
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugRoute(
  viewModel: DebugViewModel = koinViewModel(),
  popBackStack: () -> Unit,
  navigatePortalLink: (PortalLinkMode) -> Unit,
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

  viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
    when (sideEffect) {
      DebugSideEffect.PopBackStack -> popBackStack()
      is DebugSideEffect.NavigatePortalLink -> navigatePortalLink(sideEffect.mode)
    }
  }

  DebugScreen(
    uiState = uiState,
    onSelectPortalLinkStrategy = viewModel::selectPortalLinkStrategy,
    onClickOpenPortalLink = viewModel::openPortalLink,
    onClickBack = viewModel::popBackStack,
  )
}

@Composable
fun DebugScreen(
  uiState: DebugState,
  onSelectPortalLinkStrategy: (PortalLinkStrategy) -> Unit = {},
  onClickOpenPortalLink: (PortalLinkMode) -> Unit = {},
  onClickBack: () -> Unit = {},
) {
  SuwikiBackground(color = White100) {
    Column(modifier = Modifier.fillMaxSize()) {
      CchAppBarWithTitle(title = "디버그", onClickBackButton = onClickBack)

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 12.dp),
      ) {
        SectionTitle("포털 연동 경로")
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "앱을 다시 시작하면 기본으로 돌아갑니다.",
          style = CchTheme.typography.bodySm,
          color = Gray600,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          PortalLinkStrategy.entries.forEach { strategy ->
            StrategyOption(
              strategy = strategy,
              selected = strategy == uiState.portalLinkStrategy,
              onClick = { onSelectPortalLinkStrategy(strategy) },
            )
          }
        }

        Spacer(modifier = Modifier.height(32.dp))
        SectionTitle("포털 연동 화면 열기")
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "연동 여부와 관계없이 화면을 엽니다. 위에서 고른 경로로 연동합니다.",
          style = CchTheme.typography.bodySm,
          color = Gray600,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          CchBasicButton(
            text = "첫 연동 화면",
            enable = true,
            onClick = { onClickOpenPortalLink(PortalLinkMode.LINK) },
          )
          CchBasicButton(
            text = "학업 정보 업데이트 화면",
            enable = true,
            onClick = { onClickOpenPortalLink(PortalLinkMode.RESYNC) },
          )
        }
      }
    }
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text,
    style = CchTheme.typography.bodyLgStrong,
    color = Black100,
  )
}

@Composable
private fun StrategyOption(
  strategy: PortalLinkStrategy,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val shape = RoundedCornerShape(10.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(if (selected) Purple100 else White100)
      .border(width = 1.dp, color = if (selected) Purple600 else Gray300, shape = shape)
      .cchClickable(onClick = onClick)
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(20.dp)
        .clip(CircleShape)
        .border(width = 2.dp, color = if (selected) Purple600 else Gray300, shape = CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      if (selected) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(Purple600),
        )
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = strategy.title,
        style = CchTheme.typography.bodyMdStrong,
        color = Black100,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = strategy.description,
        style = CchTheme.typography.bodySm,
        color = Gray600,
      )
    }
  }
}

private val PortalLinkStrategy.title: String
  get() = when (this) {
    PortalLinkStrategy.DEVICE_FIRST -> "기본"
    PortalLinkStrategy.DEVICE_ONLY -> "앱 로직만"
    PortalLinkStrategy.SERVER_ONLY -> "서버 로직만"
  }

private val PortalLinkStrategy.description: String
  get() = when (this) {
    PortalLinkStrategy.DEVICE_FIRST -> "앱에서 스크래핑해 올리고, 기기 쪽 문제로 실패하면 서버 job으로 넘깁니다. 출시 빌드와 같습니다."
    PortalLinkStrategy.DEVICE_ONLY -> "앱에서 스크래핑해 올립니다. 실패해도 서버 job으로 넘기지 않습니다."
    PortalLinkStrategy.SERVER_ONLY -> "앱에서 스크래핑하지 않고 바로 서버 job을 만듭니다."
  }

@Preview
@Composable
private fun DebugScreenPreview() {
  CchTheme {
    DebugScreen(uiState = DebugState(portalLinkStrategy = PortalLinkStrategy.SERVER_ONLY))
  }
}
