package com.chukchukhaksa.mobile.local.database.openlecture.converter

import androidx.room.TypeConverter
import com.chukchukhaksa.mobile.local.database.openlecture.entity.CellEntity
import kotlinx.serialization.json.Json

class OpenLectureConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromCellList(value: List<CellEntity>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toCellList(value: String): List<CellEntity> {
        return json.decodeFromString(value)
    }
}