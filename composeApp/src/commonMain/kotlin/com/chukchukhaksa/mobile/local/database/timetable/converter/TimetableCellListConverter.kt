package com.chukchukhaksa.mobile.local.database.timetable.converter

import androidx.room.TypeConverter
import com.chukchukhaksa.mobile.common.model.TimetableCell
import kotlinx.serialization.json.Json

class TimetableCellListConverter {
    @TypeConverter
    fun cellListToJson(value: List<TimetableCell>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun jsonToCellList(value: String): List<TimetableCell> {
        return if (value.isBlank()) {
            emptyList()
        } else {
            Json.decodeFromString(value)
        }
    }
}
