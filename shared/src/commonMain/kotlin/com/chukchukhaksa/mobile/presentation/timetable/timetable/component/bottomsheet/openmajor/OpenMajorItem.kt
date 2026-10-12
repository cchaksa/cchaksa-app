package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.ui.cchClickable

@Composable
fun OpenMajorItem(
  modifier: Modifier = Modifier,
  text: String,
  searchValue: String = "",
  onClick: () -> Unit = {},
) {
  Row(
    modifier = modifier
      .cchClickable(
        onClick = onClick,
      )
      .padding(vertical = 16.dp, horizontal = 20.dp)
      .fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Start,
  ) {
    Text(
      text = buildHighlightedText(text, searchValue),
      style = CchTheme.typography.bodyMdStrong,
      color = Black100,
    )
  }
}

//@Preview(widthDp = 300, heightDp = 50)
//@Composable
//fun OpenMajorContainerPreview() {
//    var isChecked by remember { mutableStateOf(false) }
//
//    SuwikiTheme {
//        OpenMajorContainer(
//            text = "개설학과명",
//            isChecked = isChecked,
//            onClick = { isChecked = !isChecked },
//        )
//    }
//}

@Composable
private fun buildHighlightedText(
  text: String,
  searchValue: String,
): AnnotatedString {
  return if (searchValue.isEmpty()) {
    AnnotatedString(text)
  } else {
    buildAnnotatedString {
      val lowerText = text.lowercase()
      val lowerSearchValue = searchValue.lowercase()
      var startIndex = 0
      
      while (startIndex < text.length) {
        val index = lowerText.indexOf(lowerSearchValue, startIndex)
        if (index == -1) {
          append(text.substring(startIndex))
          break
        }
        
        // Add text before match
        if (index > startIndex) {
          append(text.substring(startIndex, index))
        }
        
        // Add highlighted match
        withStyle(style = SpanStyle(color = Purple600)) {
          append(text.substring(index, index + searchValue.length))
        }
        
        startIndex = index + searchValue.length
      }
    }
  }
}
