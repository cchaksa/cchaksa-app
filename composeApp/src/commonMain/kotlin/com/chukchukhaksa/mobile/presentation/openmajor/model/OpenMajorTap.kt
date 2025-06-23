package com.chukchukhaksa.mobile.presentation.openmajor.model

import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.word_all
import org.jetbrains.compose.resources.StringResource

enum class OpenMajorTap(
    val position: Int,
    val titleId: StringResource,
) {
    ALL(
        position = 0,
        titleId = Res.string.word_all,
    ),
}
