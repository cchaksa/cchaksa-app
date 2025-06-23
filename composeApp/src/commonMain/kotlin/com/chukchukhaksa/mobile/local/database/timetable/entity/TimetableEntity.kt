package com.chukchukhaksa.mobile.local.database.timetable.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chukchukhaksa.mobile.common.model.TimetableCell

@Entity
data class TimetableEntity(
    @PrimaryKey val createTime: Long,
    var year: String,
    var semester: String,
    var name: String,
    var cellList: List<TimetableCell>,
)
