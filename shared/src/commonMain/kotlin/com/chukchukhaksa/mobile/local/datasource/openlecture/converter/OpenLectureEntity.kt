package com.chukchukhaksa.mobile.local.datasource.openlecture.converter

import com.chukchukhaksa.mobile.common.model.Cell
import com.chukchukhaksa.mobile.common.model.OpenLecture
import com.chukchukhaksa.mobile.local.database.openlecture.entity.CellEntity
import com.chukchukhaksa.mobile.local.database.openlecture.entity.OpenLectureEntity

fun OpenLectureEntity.toModel() = OpenLecture(
  id = id,
  name = name,
  type = type,
  major = major,
  grade = grade,
  professorName = professorName,
  originalCellList = cellList.map { it.toModel() }
)

fun OpenLecture.toEntity() = OpenLectureEntity(
  id = id,
  name = name,
  type = type,
  major = major,
  grade = grade,
  professorName = professorName,
  cellList = originalCellList.map { it.toEntity() }
)

fun Cell.toEntity() = CellEntity(
  location = location,
  day = day,
  startPeriod = startPeriod,
  endPeriod = endPeriod,
)

fun CellEntity.toModel() = Cell(
  location = location,
  day = day,
  startPeriod = startPeriod,
  endPeriod = endPeriod,
)
