package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_cch_logo_ios
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray600
import org.jetbrains.compose.resources.painterResource

private const val CCHAKSA_URL = "https://www.cchaksa.com/"

@Composable
fun WebViewGuideScreen() {
  val uriHandler = LocalUriHandler.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = 178.dp, start = 20.dp, end = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Image(
      modifier = Modifier
        .size(115.dp)
        .padding(bottom = 24.dp),
      painter = painterResource(Res.drawable.ic_cch_logo_ios),
      contentDescription = "cch logo",
    )
    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 32.dp),
      text = "졸업요건은 현재 웹으로만 확인이 가능합니다.\n금방 앱에서도 만날 수 있도록 노력하겠습니다!",
      color = Gray600,
      style = CchTheme.typography.bodyMd,
      textAlign = TextAlign.Center,
    )
    CchBasicButton(
      modifier = Modifier.padding(horizontal = 38.dp),
      text = "척척학사 웹으로 이동하기",
      enable = true,
      textStyle = CchTheme.typography.bodyMdStrong,
      onClick = { uriHandler.openUri(CCHAKSA_URL) }
    )
  }
}
