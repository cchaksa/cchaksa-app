package com.chukchukhaksa.mobile.local.datasource.timetable.converter

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.local.database.timetable.entity.TimetableEntity

fun TimetableEntity.toModel() = Timetable(
    createTime = createTime,
    year = year,
    semester = semester,
    name = name,
    cellList = cellList,
)
