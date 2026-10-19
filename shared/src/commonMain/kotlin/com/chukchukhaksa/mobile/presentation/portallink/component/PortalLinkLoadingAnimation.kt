package com.chukchukhaksa.mobile.presentation.portallink.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import chukchukhaksa.composeapp.generated.resources.Res
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.time.TimeSource

// 웹 로딩 화면의 Lottie(public/videos/chuck_loading2.json)는 25fps 이미지 70장을 차례로 보여 주는 애니메이션이다.
// Lottie 라이브러리를 들이지 않고 그 프레임을 흰 배경에 합성한 600px webp로 옮겨 같은 속도로 재생한다.
private const val FRAME_COUNT = 70
private const val FRAME_DURATION_MS = 40L

private fun framePath(index: Int): String =
  "files/portal_link_loading/frame_${index.toString().padStart(2, '0')}.webp"

/**
 * 포털 연동 로딩 캐릭터 애니메이션.
 * painterResource는 디코딩한 이미지를 앱이 끝날 때까지 캐시하므로, 압축된 바이트만 들고 있다가
 * 그릴 프레임만 그때그때 디코딩한다. 프레임을 읽지 못하면 빈 영역으로 두고 안내 문구만 보여 준다(웹과 같다).
 */
@Composable
fun PortalLinkLoadingAnimation(modifier: Modifier = Modifier) {
  var frame by remember { mutableStateOf<ImageBitmap?>(null) }

  LaunchedEffect(Unit) {
    val frames = try {
      List(FRAME_COUNT) { index -> Res.readBytes(framePath(index)) }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Napier.w(tag = "PortalLinkLoading", throwable = e) { "로딩 애니메이션 프레임을 읽지 못했습니다" }
      return@LaunchedEffect
    }

    var index = 0
    while (isActive) {
      val mark = TimeSource.Monotonic.markNow()
      // 프레임 시계를 한 번 기다린다. 앱이 백그라운드에 있어 화면을 그리지 않는 동안에는 여기서 멈춰 디코딩하지 않는다.
      withFrameMillis { }
      frame = withContext(Dispatchers.Default) { frames[index].decodeToImageBitmap() }
      index = (index + 1) % FRAME_COUNT
      delay((FRAME_DURATION_MS - mark.elapsedNow().inWholeMilliseconds).coerceAtLeast(0))
    }
  }

  Box(modifier = modifier) {
    frame?.let { bitmap ->
      Image(modifier = Modifier.matchParentSize(), bitmap = bitmap, contentDescription = null)
    }
  }
}
