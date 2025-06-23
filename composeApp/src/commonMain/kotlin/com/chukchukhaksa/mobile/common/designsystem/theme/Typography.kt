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
import org.jetbrains.compose.resources.Font

@Composable
fun notoSansFamily() = FontFamily(
    Font(Res.font.notosanskrbold, FontWeight.Bold),
    Font(Res.font.notosanskrmedium, FontWeight.Medium),
    Font(Res.font.notosanskrregular, FontWeight.Normal),
    Font(Res.font.notosanskrlight, FontWeight.Light),
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
