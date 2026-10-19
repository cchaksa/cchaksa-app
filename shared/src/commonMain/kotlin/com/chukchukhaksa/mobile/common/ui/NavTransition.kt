package com.chukchukhaksa.mobile.common.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

// Android와 iOS가 같은 전환을 쓴다. 들어오는 화면을 처음 구성하느라 첫 프레임이 늦어지므로,
// 처음엔 빠르고 끝에선 느려지는 곡선으로 조금 길게 움직여 그 지연이 덜 보이게 한다.
private const val NAV_TRANSITION_DURATION_MS = 350

/** 전환 중 아래 화면이 밀려나는 거리(화면 폭 대비). navigation-compose 2.9 iOS 기본 전환과 같은 값이다. */
private const val NAV_PARALLAX = 0.3f

/** 전환 중 아래 화면을 덮는 dim의 최대 농도. navigation-compose 2.9 iOS 기본 전환과 같은 값이다. */
private const val NAV_DIM_MAX_ALPHA = 0.106f

private fun navTween() = tween<IntOffset>(NAV_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)

/**
 * 뒤로가기 전환은 직선으로 움직인다. iOS 스와이프 뒤로가기는 손가락 위치를 전환 진행률로 바로 쓰는데,
 * 곡선을 쓰면 그 진행률이 곡선을 거쳐 화면이 손가락보다 앞서거나 뒤처진다.
 */
private fun navPopTween() = tween<IntOffset>(NAV_TRANSITION_DURATION_MS, easing = LinearEasing)

val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
  slideInHorizontally(navTween()) { it }
}
val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
  slideOutHorizontally(navTween()) { -(it * NAV_PARALLAX).toInt() }
}
val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
  slideInHorizontally(navPopTween()) { -(it * NAV_PARALLAX).toInt() }
}
val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
  slideOutHorizontally(navPopTween()) { it }
}

/**
 * [composable]과 같지만, 화면 전환 중 아래 화면에 dim을 덮는다.
 *
 * navigation-compose 2.9의 iOS 기본 전환도 dim을 그리지만, 뒤로가기 애니메이션이 끝나는 프레임에
 * dim을 최대 농도로 한 번 그려 화면이 검게 깜빡인다. 그래서 App의 NavHost는 전환을 직접 지정해
 * 기본 dim을 끄고, 목적지마다 이 함수로 dim을 그린다.
 */
fun NavGraphBuilder.cchComposable(
  route: String,
  arguments: List<NamedNavArgument> = emptyList(),
  content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
  composable(route = route, arguments = arguments) { backStackEntry ->
    val animatedContentScope = this
    Box(modifier = Modifier.navTransitionDim()) {
      animatedContentScope.content(backStackEntry)
    }
  }
}

/**
 * 화면이 왼쪽으로 밀려난 만큼 위에 검은 dim을 그린다. 아래 화면만 왼쪽으로 밀려나므로 위 화면에는 그려지지 않는다.
 *
 * 전환 상태 대신 실제 위치로 농도를 정하므로, 화면이 제자리에 오는 순간 dim도 0이 되고 스와이프 뒤로가기도 따라간다.
 * alpha를 낮추는 대신 사각형을 덮어 그려야 네이티브 뷰(UIKitView) 영역도 함께 어두워진다.
 */
private fun Modifier.navTransitionDim(): Modifier = this then NavTransitionDimElement

private data object NavTransitionDimElement : ModifierNodeElement<NavTransitionDimNode>() {
  override fun create() = NavTransitionDimNode()

  override fun update(node: NavTransitionDimNode) = Unit
}

private class NavTransitionDimNode :
  Modifier.Node(),
  GlobalPositionAwareModifierNode,
  DrawModifierNode {

  private var offsetX = 0f

  override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
    val x = coordinates.positionInRoot().x
    if (x != offsetX) {
      offsetX = x
      invalidateDraw()
    }
  }

  override fun ContentDrawScope.draw() {
    drawContent()
    val travel = size.width * NAV_PARALLAX
    if (travel <= 0f) return
    val fraction = (-offsetX / travel).coerceIn(0f, 1f)
    if (fraction > 0f) {
      drawRect(color = Color.Black, alpha = NAV_DIM_MAX_ALPHA * fraction)
    }
  }
}
