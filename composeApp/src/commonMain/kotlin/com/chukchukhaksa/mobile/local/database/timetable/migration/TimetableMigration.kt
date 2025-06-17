package com.chukchukhaksa.mobile.local.common.database.timetable.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableDay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val TIMETABLE_MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(database: SQLiteConnection) {
        database.execSQL(
            """
      CREATE TABLE IF NOT EXISTS TimetableEntity (
      `createTime` INTEGER NOT NULL,
      `year` TEXT NOT NULL,
      `semester` TEXT NOT NULL,
      `name` TEXT NOT NULL,
      `cellList` TEXT NOT NULL,
      PRIMARY KEY(`createTime`)
      )
      """.trimIndent(),
        )

        val cursor = database.prepare("SELECT * FROM TimetableList").use { statement ->
            statement.step()
        }

        while (cursor) {
            val createTime =
                database.prepare("SELECT createTime FROM TimetableList").use { it.getLong(0) }
            val year = database.prepare("SELECT year FROM TimetableList").use { it.getText(0) }
            val semester =
                database.prepare("SELECT semester FROM TimetableList").use { it.getText(0) }
            val timeTableName =
                database.prepare("SELECT timeTableName FROM TimetableList").use { it.getText(0) }
            val timeTableJsonData = database.prepare("SELECT timeTableJsonData FROM TimetableList")
                .use { it.getText(0) }

            val cellList = timeTableJsonDataToCellList(timeTableJsonData)

            database.execSQL(
                """
          INSERT INTO TimetableEntity (createTime, year, semester, name, cellList)
          VALUES ($createTime, '$year', '$semester', '$timeTableName', '$cellList')
          """.trimIndent(),
            )
        }

        database.execSQL("DROP TABLE TimeTableList")
    }
}

@Serializable
data class LegacyTimeTableCell(
    val name: String = "",
    val professor: String = "",
    val location: String = "",
    val day: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val color: Int,
    val credit: String = "",
)

private val legacyColorMap: Map<Int, TimetableCellColor> by lazy {
    mapOf(
        -96120 to TimetableCellColor.PINK,
        -16046 to TimetableCellColor.ORANGE,
        -3368205 to TimetableCellColor.VIOLET,
        -7747330 to TimetableCellColor.SKY,
        -5907327 to TimetableCellColor.GREEN,
        -4026526 to TimetableCellColor.BROWN,
        -4013635 to TimetableCellColor.GRAY,
        -12363882 to TimetableCellColor.NAVY,
        -9728172 to TimetableCellColor.GREEN_DARK,
        -17536 to TimetableCellColor.BROWN_LIGHT,
        -6194752 to TimetableCellColor.PURPLE,
        -7369077 to TimetableCellColor.GRAY_DARK,
    )
}

fun LegacyTimeTableCell.toTimetableCell(): TimetableCell {
    val color = legacyColorMap.getOrElse(
        this.color
    ) {
        TimetableCellColor.entries.shuffled().first()
    }

    val day = when (this.day) {
        "월" -> TimetableDay.MON
        "화" -> TimetableDay.TUE
        "수" -> TimetableDay.WED
        "목" -> TimetableDay.THU
        "금" -> TimetableDay.FRI
        "토" -> TimetableDay.SAT
        "이러닝" -> TimetableDay.E_LEARNING
        else -> TimetableDay.E_LEARNING
    }

    return TimetableCell(
        name = name,
        professor = professor,
        location = location,
        day = day,
        startPeriod = startTime.toIntOrNull() ?: 0,
        endPeriod = endTime.toIntOrNull() ?: 0,
        color = color,
    )
}

fun timeTableJsonDataToCellList(timeTableJsonData: String): String {
    if (timeTableJsonData.isBlank()) return ""
    val legacy = Json.decodeFromString<List<LegacyTimeTableCell>>(timeTableJsonData)
    val cellList = legacy.map { it.toTimetableCell() }
    return Json.encodeToString(cellList)
}
