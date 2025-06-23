package com.chukchukhaksa.mobile.presentation.openmajor.model

import kotlinx.collections.immutable.toPersistentList
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class OpenMajor(
    val id: Uuid = Uuid.random(),
    val name: String,
    val isSelected: Boolean = false,
)

@OptIn(ExperimentalUuidApi::class)
fun List<String>.toBookmarkedOpenMajorList(
    searchValue: String,
    selectedOpenMajor: String,
) = filter { openMajor ->
    if (searchValue.isNotEmpty()) {
        searchValue in openMajor
    } else {
        true
    }
}.map { name ->
    OpenMajor(
        name = name,
        isSelected = selectedOpenMajor == name,
    )
}.toPersistentList()

@OptIn(ExperimentalUuidApi::class)
fun List<String>.toOpenMajorList(
    searchValue: String,
    selectedOpenMajor: String,
) = filter { openMajor ->
    if (searchValue.isNotEmpty()) {
        searchValue in openMajor
    } else {
        true
    }
}.map { name ->
    OpenMajor(
        name = name,
        isSelected = selectedOpenMajor == name,
    )
}.toPersistentList()
