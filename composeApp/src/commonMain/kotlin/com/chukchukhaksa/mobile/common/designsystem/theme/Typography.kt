package com.chukchukhaksa.mobile.common.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.notosanskrbold
import chukchukhaksa.composeapp.generated.resources.notosanskrlight
import chukchukhaksa.composeapp.generated.resources.notosanskrmedium
import chukchukhaksa.composeapp.generated.resources.notosanskrregular
import chukchukhaksa.composeapp.generated.resources.paperlogybold
import chukchukhaksa.composeapp.generated.resources.suitbold
import chukchukhaksa.composeapp.generated.resources.suitextrabold
import chukchukhaksa.composeapp.generated.resources.suitmedium
import chukchukhaksa.composeapp.generated.resources.suitregular
import org.jetbrains.compose.resources.Font

@Composable
fun notoSansFamily() = FontFamily(
  Font(Res.font.notosanskrbold, FontWeight.Bold),
  Font(Res.font.notosanskrmedium, FontWeight.Medium),
  Font(Res.font.notosanskrregular, FontWeight.Normal),
  Font(Res.font.notosanskrlight, FontWeight.Light),
)

@Composable
fun paperlogyFamily() = FontFamily(
  Font(Res.font.paperlogybold, FontWeight.Bold),
)

@Composable
fun suitFamily() = FontFamily(
  Font(Res.font.suitextrabold, FontWeight.ExtraBold),
  Font(Res.font.suitbold, FontWeight.Bold),
  Font(Res.font.suitmedium, FontWeight.Medium),
  Font(Res.font.suitregular, FontWeight.Normal),
)

@Composable
private fun notoSansStyle() = TextStyle(
  fontFamily = notoSansFamily(),
  lineHeight = 1.5.em,
  lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
  ),
  color = Black,
)

@Composable
private fun paperlogyStyle() = TextStyle(
  fontFamily = paperlogyFamily(),
  lineHeight = 1.5.em,
  letterSpacing = (-0.01).em,
  lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
  ),
  color = Black,
)

@Composable
private fun suitStyle() = TextStyle(
  fontFamily = suitFamily(),
  lineHeight = 1.6.em,
  letterSpacing = (-0.02).em,
  lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
  ),
  color = Black,
)

@Composable
fun Typography(): SuwikiTypography {
  val notoSansStyle = notoSansStyle()

  return SuwikiTypography(
    header1 = notoSansStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp,
    ),
    header2 = notoSansStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp,
    ),
    header3 = notoSansStyle.copy(
      fontWeight = FontWeight.Medium,
      fontSize = 18.sp,
    ),
    header4 = notoSansStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 18.sp,
    ),
    header5 = notoSansStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
    ),
    header6 = notoSansStyle.copy(
      fontWeight = FontWeight.Medium,
      fontSize = 16.sp,
    ),
    header7 = notoSansStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
    ),

    body1 = notoSansStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
    ),
    body2 = notoSansStyle.copy(
      fontWeight = FontWeight.Medium,
      fontSize = 15.sp,
    ),
    body3 = notoSansStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 15.sp,
    ),
    body4 = notoSansStyle.copy(
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
    ),
    body5 = notoSansStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
    ),
    body6 = notoSansStyle.copy(
      fontWeight = FontWeight.Medium,
      fontSize = 13.sp,
    ),
    body7 = notoSansStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 13.sp,
    ),

    caption1 = notoSansStyle.copy(
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
    ),
    caption2 = notoSansStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
    ),
    caption3 = notoSansStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 11.sp,
    ),
    caption4 = notoSansStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 11.sp,
    ),
    caption5 = notoSansStyle.copy(
      fontWeight = FontWeight.Medium,
      fontSize = 10.sp,
    ),
    caption6 = notoSansStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 10.sp,
    ),
    caption7 = notoSansStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 8.sp,
    ),
  )
}

@Composable
fun CCHaksaTypography(): CCHaksaTypography {
  val paperlogyStyle = paperlogyStyle()
  val suitStyle = suitStyle()

  return CCHaksaTypography(
    titleExlg2 = paperlogyStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp,
    ),
    titleExlg = paperlogyStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 28.sp,
    ),
    titleLg = paperlogyStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 24.sp,
    ),

    bodyExlg = suitStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 24.sp,
    ),
    bodyLgStrong = suitStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp,
    ),
    bodyLg = suitStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 18.sp,
    ),
    bodyMdStrong = suitStyle.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
    ),
    bodyMd = suitStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
    ),
    bodySmStrong = suitStyle.copy(
      fontWeight = FontWeight.ExtraBold,
      fontSize = 14.sp,
    ),
    bodySm = suitStyle.copy(
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
    ),
    bodyXs = suitStyle.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
    ),
  )
}

data class CCHaksaTypography(
  val titleExlg2: TextStyle,
  val titleExlg: TextStyle,
  val titleLg: TextStyle,

  val bodyExlg: TextStyle,
  val bodyLgStrong: TextStyle,
  val bodyLg: TextStyle,
  val bodyMdStrong: TextStyle,
  val bodyMd: TextStyle,
  val bodySmStrong: TextStyle,
  val bodySm: TextStyle,
  val bodyXs: TextStyle,
)

data class SuwikiTypography(
  val header1: TextStyle,
  val header2: TextStyle,
  val header3: TextStyle,
  val header4: TextStyle,
  val header5: TextStyle,
  val header6: TextStyle,
  val header7: TextStyle,

  val body1: TextStyle,
  val body2: TextStyle,
  val body3: TextStyle,
  val body4: TextStyle,
  val body5: TextStyle,
  val body6: TextStyle,
  val body7: TextStyle,

  val caption1: TextStyle,
  val caption2: TextStyle,
  val caption3: TextStyle,
  val caption4: TextStyle,
  val caption5: TextStyle,
  val caption6: TextStyle,
  val caption7: TextStyle,
)

val LocalTypography = staticCompositionLocalOf {
  SuwikiTypography(
    header1 = TextStyle.Default,
    header2 = TextStyle.Default,
    header3 = TextStyle.Default,
    header4 = TextStyle.Default,
    header5 = TextStyle.Default,
    header6 = TextStyle.Default,
    header7 = TextStyle.Default,
    body1 = TextStyle.Default,
    body2 = TextStyle.Default,
    body3 = TextStyle.Default,
    body4 = TextStyle.Default,
    body5 = TextStyle.Default,
    body6 = TextStyle.Default,
    body7 = TextStyle.Default,
    caption1 = TextStyle.Default,
    caption2 = TextStyle.Default,
    caption3 = TextStyle.Default,
    caption4 = TextStyle.Default,
    caption5 = TextStyle.Default,
    caption6 = TextStyle.Default,
    caption7 = TextStyle.Default,
  )
}

val LocalCCHaksaTypography = staticCompositionLocalOf {
  CCHaksaTypography(
    titleExlg2 = TextStyle.Default,
    titleExlg = TextStyle.Default,
    titleLg = TextStyle.Default,
    bodyExlg = TextStyle.Default,
    bodyLgStrong = TextStyle.Default,
    bodyLg = TextStyle.Default,
    bodyMdStrong = TextStyle.Default,
    bodyMd = TextStyle.Default,
    bodySmStrong = TextStyle.Default,
    bodySm = TextStyle.Default,
    bodyXs = TextStyle.Default,
  )
}
