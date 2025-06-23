package com.chukchukhaksa.mobile.local.database.openlecture.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chukchukhaksa.mobile.common.model.TimetableDay
import kotlinx.serialization.Serializable

@Entity
data class OpenLectureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val major: String,
    val grade: Int,
    val professorName: String,
    val cellList: List<CellEntity> = emptyList()
)

@Entity
@Serializable
data class CellEntity(
    val location: String,
    val day: TimetableDay,
    val startPeriod: Int,
    val endPeriod: Int
)