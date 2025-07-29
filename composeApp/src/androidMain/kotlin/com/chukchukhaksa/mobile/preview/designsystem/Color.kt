package com.chukchukhaksa.mobile.preview.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CCHaksaTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray200
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray300
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray500
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray600
import com.chukchukhaksa.mobile.common.designsystem.theme.Green100
import com.chukchukhaksa.mobile.common.designsystem.theme.Green200
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple100
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple200
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple300
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple400
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple500
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.Red100
import com.chukchukhaksa.mobile.common.designsystem.theme.Red200
import com.chukchukhaksa.mobile.common.designsystem.theme.Red300
import com.chukchukhaksa.mobile.common.designsystem.theme.Red400
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.designsystem.theme.Yellow100
import com.chukchukhaksa.mobile.common.designsystem.theme.Yellow200

@Composable
fun ColorItem(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(40.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(8.dp),
                ),
        )

        Text(
            text = name,
            style = CchTheme.typography.bodyMd,
            color = Black100,
            fontSize = 14.sp,
        )
    }
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
fun CCHaksaColorPreview() {
    CCHaksaTheme {
        Column(
            modifier = Modifier
                .background(White100)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Cool Gray",
                style = CchTheme.typography.titleLg,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            ColorItem("Gray200", Gray200)
            ColorItem("Gray300", Gray300)
            ColorItem("Gray400", Gray400)
            ColorItem("Gray500", Gray500)
            ColorItem("Gray600", Gray600)

            Text(
                text = "Purple",
                style = CchTheme.typography.titleLg,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            ColorItem("Purple100", Purple100)
            ColorItem("Purple200", Purple200)
            ColorItem("Purple300", Purple300)
            ColorItem("Purple400", Purple400)
            ColorItem("Purple500", Purple500)
            ColorItem("Purple600", Purple600)

            Text(
                text = "Red",
                style = CchTheme.typography.titleLg,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            ColorItem("Red100", Red100)
            ColorItem("Red200", Red200)
            ColorItem("Red300", Red300)
            ColorItem("Red400", Red400)

            Text(
                text = "Yellow",
                style = CchTheme.typography.titleLg,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            ColorItem("Yellow100", Yellow100)
            ColorItem("Yellow200", Yellow200)

            Text(
                text = "Green",
                style = CchTheme.typography.titleLg,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            ColorItem("Green100", Green100)
            ColorItem("Green200", Green200)
        }
    }
}
