package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor.model

import kotlinx.collections.immutable.toPersistentList
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class OpenMajor(
    val id: Uuid = Uuid.random(),
    val name: String?,
    val isSelected: Boolean = false,
)

@OptIn(ExperimentalUuidApi::class)
fun List<String?>.toOpenMajorList(
    searchValue: String,
    selectedOpenMajor: String?,
) = filter { openMajor ->
    if (searchValue.isNotEmpty()) {
      openMajor?.contains(searchValue) == true
    } else {
        true
    }
}.map { name ->
    OpenMajor(
        name = name,
        isSelected = selectedOpenMajor == name,
    )
}.toPersistentList()
