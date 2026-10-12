package com.chukchukhaksa.mobile.common.designsystem.component.searchbar

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.GrayF6
import com.chukchukhaksa.mobile.common.designsystem.theme.White

@Composable
fun SuwikiSearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = "",
    value: String = "",
    maxLines: Int = 1,
    minLines: Int = 1,
    onValueChange: (String) -> Unit = { _ -> },
    onClickClearButton: () -> Unit = {},
    onClickSearchButton: (String) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Box(
        modifier
            .background(White)
            .padding(horizontal = 24.dp)
            .height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicSearchBar(
            modifier = Modifier
                .background(GrayF6, shape = RoundedCornerShape(10.dp))
                .padding(8.dp),
            value = value,
            onValueChange = onValueChange,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            placeholder = placeholder,
            placeholderColor = Gray95,
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(
                onDone = {
                    onClickSearchButton(value)
                    keyboardController?.hide()
                },
            ),
            onClickClearButton = onClickClearButton,
        )
    }
}
