package com.chukchukhaksa.mobile.presentation.timetable.timetable.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.ic_cch_logo_ios
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray600
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.ui.cchClickable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val CCHAKSA_URL = "https://www.cchaksa.com/"
private const val CCHAKSA_ASK_URL = "https://heliotrope-flea-959.notion.site/24d1a2480da1805d9d9bcaf608fb629b"

@Preview
@Composable
fun WebViewGuideScreen() {
  val uriHandler = LocalUriHandler.current

  SuwikiBackground {
    Box {
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
          onClick = { uriHandler.openUri(CCHAKSA_URL) },
        )
      }

      Text(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 16.dp)
          .cchClickable { uriHandler.openUri(CCHAKSA_ASK_URL) },
        text = "척척학사에 문의하기",
        style = CchTheme.typography.bodyMdStrong.copy(
          color = Purple600,
          textDecoration = TextDecoration.Underline,
        ),
      )
    }
  }
}
