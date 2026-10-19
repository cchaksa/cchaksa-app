package com.chukchukhaksa.mobile.presentation.portallink.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_portal_link_arrow_left
import chukchukhaksa.composeapp.generated.resources.ic_portal_link_delete
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray300
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray500
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple200
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.ui.cchClickable
import org.jetbrains.compose.resources.painterResource

// 포털 연동 화면은 웹 컴포넌트(components/ui의 TextField·FixedButton·ConfirmDialog·TopNavigation)를 그대로 옮겼다.
// 앱 디자인 시스템의 같은 이름 컴포넌트와 상태별 모양이 달라서 이 패키지 안에만 둔다.

// 앱 테마에 없는 웹 색상 토큰(styles/color.scss).
private val WebGray300 = Color(0xFFEFF0F7)
private val WebRed200 = Color(0xFFFEAFAC)

// CSS `ease`. 웹은 상태 전환과 문구 페이드에 이 곡선을 쓴다.
internal val CssEase = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

private const val WORD_JOINER = '⁠'

/**
 * 웹은 본문 전체에 `word-break: keep-all`을 둔다. Compose는 한글을 음절 사이에서도 줄바꿈하므로,
 * 붙어 있는 한글 음절 사이에 WORD JOINER(U+2060, 보이지 않는 문자)를 넣어 띄어쓰기에서만 줄이 바뀌게 한다.
 */
internal fun String.keepAll(): String = buildString(length * 2) {
  this@keepAll.forEachIndexed { index, char ->
    if (index > 0 && char.isHangulSyllable() && this@keepAll[index - 1].isHangulSyllable()) {
      append(WORD_JOINER)
    }
    append(char)
  }
}

private fun Char.isHangulSyllable(): Boolean = this in '가'..'힣'

/** 웹 TextField. 입력 전에는 회색 바탕, 포커스·입력 완료·오류에 따라 테두리가 바뀌고, 값이 있으면 지우기 버튼이 나온다. */
@Composable
internal fun PortalLinkTextField(
  value: String,
  placeholder: String,
  isError: Boolean,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  visualTransformation: VisualTransformation = VisualTransformation.None,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val (targetBackground, targetBorder) = when {
    isError -> White100 to WebRed200
    isFocused -> White100 to Purple600
    value.isNotEmpty() -> White100 to Purple200
    else -> WebGray300 to Color.Transparent
  }
  // 웹은 상태가 바뀔 때 `transition: all 0.2s ease`로 색을 바꾼다.
  val background by animateColorAsState(targetBackground, tween(200, easing = CssEase))
  val border by animateColorAsState(targetBorder, tween(200, easing = CssEase))
  val shape = RoundedCornerShape(10.dp)

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier.fillMaxWidth(),
    singleLine = true,
    textStyle = CchTheme.typography.bodyLgStrong.copy(color = Black100),
    cursorBrush = SolidColor(Black100),
    keyboardOptions = keyboardOptions,
    visualTransformation = visualTransformation,
    interactionSource = interactionSource,
    decorationBox = { innerTextField ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp)
          .clip(shape)
          .background(background)
          .border(width = 1.dp, color = border, shape = shape)
          .padding(start = 20.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(modifier = Modifier.weight(1f)) {
          if (value.isEmpty()) {
            Text(
              text = placeholder,
              // 웹 placeholder는 font-weight 300이다. 앱에 SUIT Light가 없어 가장 가까운 Regular로 그려진다.
              style = CchTheme.typography.bodyLg.copy(fontWeight = FontWeight.Light),
              color = Gray400,
              maxLines = 1,
            )
          }
          innerTextField()
        }
        if (value.isNotEmpty()) {
          Spacer(modifier = Modifier.width(8.dp))
          Image(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .cchClickable(onClick = { onValueChange("") }),
            painter = painterResource(Res.drawable.ic_portal_link_delete),
            contentDescription = "입력 내용 삭제",
          )
        }
      }
    },
  )
}

/** 웹 FixedButton(primary, md). 비활성일 때 색을 바꾸지 않고 반투명하게 그린다. */
@Composable
internal fun PortalLinkButton(
  text: String,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(10.dp)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp)
      .alpha(if (enabled) 1f else 0.5f)
      .clip(shape)
      .background(Black100)
      .then(if (enabled) Modifier.cchClickable(onClick = onClick) else Modifier)
      .padding(horizontal = 18.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      style = CchTheme.typography.bodyLgStrong,
      color = White100,
    )
  }
}

/** 웹 TopNavigation.Preset(type="back", title=""). */
@Composable
internal fun PortalLinkTopBar(onClickBack: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(52.dp)
      .padding(start = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .cchClickable(onClick = onClickBack),
      painter = painterResource(Res.drawable.ic_portal_link_arrow_left),
      contentDescription = "뒤로가기",
    )
  }
}

/** 웹 ConfirmDialog. 제목을 넘기지 않으면 웹과 같이 "알림"이다. */
@Composable
internal fun PortalLinkConfirmDialog(
  message: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  title: String = "알림",
  confirmText: String = "확인",
  cancelText: String = "취소",
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    val shape = RoundedCornerShape(16.dp)
    Column(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .widthIn(max = 320.dp)
        .fillMaxWidth()
        // box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15)
        .shadow(elevation = 12.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.15f))
        .clip(shape)
        .background(White100)
        .padding(20.dp),
    ) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = title,
        style = CchTheme.typography.bodyLgStrong,
        color = Black100,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(20.dp))
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = message.keepAll(),
        style = CchTheme.typography.bodyMd,
        color = Gray500,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(20.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DialogButton(
          modifier = Modifier.weight(1f),
          text = cancelText,
          background = White100,
          textColor = Black100,
          borderColor = Gray300,
          onClick = onDismiss,
        )
        DialogButton(
          modifier = Modifier.weight(1f),
          text = confirmText,
          background = Black100,
          textColor = White100,
          borderColor = null,
          onClick = onConfirm,
        )
      }
    }
  }
}

@Composable
private fun DialogButton(
  text: String,
  background: Color,
  textColor: Color,
  borderColor: Color?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(10.dp)
  Box(
    modifier = modifier
      .clip(shape)
      .background(background)
      .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
      .cchClickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      // 웹은 font-weight 600이다. 앱에 SUIT SemiBold가 없어 가장 가까운 Bold로 그려진다.
      style = CchTheme.typography.bodyMd.copy(fontWeight = FontWeight.SemiBold),
      color = textColor,
    )
  }
}
