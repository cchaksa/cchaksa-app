package com.chukchukhaksa.mobile.local.datasource.openmajor.converter

import com.chukchukhaksa.mobile.common.model.OpenMajor
import com.chukchukhaksa.mobile.local.database.openmajor.entity.OpenMajorEntity

fun OpenMajorEntity.toModel() = OpenMajor(
  id = id,
  name = name,
)

fun OpenMajor.toEntity() = OpenMajorEntity(name)
