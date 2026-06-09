package com.chukchukhaksa.mobile.common.designsystem.component.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray100
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 홈 웹뷰가 최초 로드되기 전 표시하는 스켈레톤 화면.
 * [CircularProgressIndicator] 대신 콘텐츠 형태의 shimmer를 보여줘 체감 로딩을 부드럽게 한다.
 */
@Composable
fun WebViewLoadingShimmer(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(White100)
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(horizontal = 16.dp, vertical = 16.dp),
  ) {
    // 인사/타이틀
    Spacer(Modifier.height(16.dp))
    Box(Modifier.fillMaxWidth(0.5f).height(26.dp).shimmer())
    Spacer(Modifier.height(8.dp))
    Box(Modifier.fillMaxWidth(0.35f).height(16.dp).shimmer())

    Spacer(Modifier.height(16.dp))

    // 히어로 배너
    Box(Modifier.fillMaxWidth().height(150.dp).shimmer(RoundedCornerShape(16.dp)))

    Spacer(Modifier.height(8.dp))

    // 퀵 액션 카드 2개
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(Modifier.weight(1f).height(96.dp).shimmer(RoundedCornerShape(12.dp)))
    }

    Spacer(Modifier.height(16.dp))

    // 섹션 타이틀
    Box(Modifier.fillMaxWidth(0.4f).height(20.dp).shimmer())
    Spacer(Modifier.height(8.dp))

    // 퀵 액션 카드 2개
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(Modifier.weight(1f).height(96.dp).shimmer(RoundedCornerShape(12.dp)))
    }

    Spacer(Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(Modifier.weight(1f).height(96.dp).shimmer(RoundedCornerShape(12.dp)))
    }
  }
}

@Composable
private fun ShimmerListRow() {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(Modifier.size(48.dp).shimmer(RoundedCornerShape(12.dp)))
    Spacer(Modifier.width(12.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(Modifier.fillMaxWidth(0.7f).height(16.dp).shimmer())
      Box(Modifier.fillMaxWidth(0.45f).height(14.dp).shimmer())
    }
  }
}

/**
 * 주어진 [shape]로 잘라낸 영역에 좌→우로 흐르는 shimmer 그라데이션을 그린다.
 * 각 요소가 자신의 크기에 맞춰 애니메이션하도록 [onGloballyPositioned]로 크기를 측정한다.
 */
private fun Modifier.shimmer(shape: Shape = RoundedCornerShape(8.dp)): Modifier = composed {
  var size by remember { mutableStateOf(IntSize.Zero) }
  val transition = rememberInfiniteTransition(label = "shimmer")
  val startOffsetX by transition.animateFloat(
    initialValue = -2f * size.width,
    targetValue = 2f * size.width,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "shimmerOffsetX",
  )

  clip(shape)
    .background(
      brush = Brush.linearGradient(
        colors = listOf(Gray200, Gray100, Gray200),
        start = Offset(startOffsetX, 0f),
        end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat()),
      ),
    )
    .onGloballyPositioned { size = it.size }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun WebViewLoadingShimmerPreview() {
  CchTheme {
    WebViewLoadingShimmer()
  }
}
