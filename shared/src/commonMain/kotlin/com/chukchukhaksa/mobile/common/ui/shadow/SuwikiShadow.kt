package com.chukchukhaksa.mobile.common.ui.shadow

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.kmp.setMaskFilter

fun Modifier.suwikiShadow(
  color: Color = Color.Black,
  borderRadius: Dp = 0.dp,
  blurRadius: Dp = 0.dp,
  offsetY: Dp = 0.dp,
  offsetX: Dp = 0.dp,
  spread: Dp = 0f.dp,
  modifier: Modifier = Modifier,
) = this.then(
  modifier.drawBehind {
    this.drawIntoCanvas {
      val paint = Paint()
      paint.color = color
      val frameworkPaint = paint.asFrameworkPaint()
      val spreadPixel = spread.toPx()
      val leftPixel = (0f - spreadPixel) + offsetX.toPx()
      val topPixel = (0f - spreadPixel) + offsetY.toPx()
      val rightPixel = (this.size.width + spreadPixel)
      val bottomPixel = (this.size.height + spreadPixel)

      if (blurRadius != 0.dp) {
        frameworkPaint.setMaskFilter(blurRadius.toPx())
      }

      it.drawRoundRect(
        left = leftPixel,
        top = topPixel,
        right = rightPixel,
        bottom = bottomPixel,
        radiusX = borderRadius.toPx(),
        radiusY = borderRadius.toPx(),
        paint,
      )
    }
  },
)