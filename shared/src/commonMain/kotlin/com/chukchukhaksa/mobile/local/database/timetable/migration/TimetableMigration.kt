package com.chukchukhaksa.mobile.local.database.timetable.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.model.TimetableCellColor
import com.chukchukhaksa.mobile.common.model.TimetableDay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val LEGACY_TABLE_NAME = "TimeTableList"

/**
 * 버전 1의 `TimeTableList`를 버전 2의 `TimetableEntity`로 옮긴다.
 *
 * 셀 변환에 실패하더라도 스키마 전환 자체는 완료시킨다. 마이그레이션이 예외를 던지면 Room이
 * 트랜잭션을 롤백하여 DB 버전이 1에 머무르고, 그 사용자는 앱을 다시 실행해도 같은 지점에서
 * 영구히 실패하기 때문이다. 변환에 실패한 경우에는 레거시 테이블을 지우지 않고 남겨 두어
 * 이후에 복구할 여지를 둔다.
 */
val TIMETABLE_MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `TimetableEntity` (
            `createTime` INTEGER NOT NULL,
            `year` TEXT NOT NULL,
            `semester` TEXT NOT NULL,
            `name` TEXT NOT NULL,
            `cellList` TEXT NOT NULL,
            PRIMARY KEY(`createTime`)
            )
            """.trimIndent(),
        )

        if (!connection.hasLegacyTimetableTable()) return

        val copied = runCatching { connection.copyLegacyTimetables() }.getOrDefault(false)

        if (copied) {
            connection.execSQL("DROP TABLE IF EXISTS `$LEGACY_TABLE_NAME`")
        }
    }
}

private fun SQLiteConnection.hasLegacyTimetableTable(): Boolean =
    prepare("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? COLLATE NOCASE")
        .use { statement ->
            statement.bindText(1, LEGACY_TABLE_NAME)
            statement.step()
        }

/**
 * 레거시 행을 새 테이블로 옮기고, 모든 셀 목록이 온전히 변환됐는지를 돌려준다.
 *
 * 셀 변환은 [timeTableJsonDataToCellList] 안에서 예외를 삼키고 빈 문자열을 돌려주므로, SQL 이
 * 성공했다는 사실만으로는 데이터가 보존됐다고 말할 수 없다. 한 행이라도 변환에 실패했다면
 * `false` 를 돌려주어 호출자가 레거시 테이블을 지우지 않도록 한다.
 */
private fun SQLiteConnection.copyLegacyTimetables(): Boolean {
    val legacyTimetables = prepare(
        "SELECT `createTime`, `year`, `semester`, `timeTableName`, `timeTableJsonData` " +
            "FROM `$LEGACY_TABLE_NAME`",
    ).use { statement ->
        buildList {
            while (statement.step()) {
                add(
                    LegacyTimetable(
                        createTime = statement.getLong(0),
                        year = statement.getText(1),
                        semester = statement.getText(2),
                        name = statement.getText(3),
                        cellListJson = statement.getText(4),
                    ),
                )
            }
        }
    }

    var allCellsConverted = true

    prepare(
        "INSERT OR REPLACE INTO `TimetableEntity` " +
            "(`createTime`, `year`, `semester`, `name`, `cellList`) VALUES (?, ?, ?, ?, ?)",
    ).use { statement ->
        legacyTimetables.forEach { timetable ->
            val cellList = timeTableJsonDataToCellList(timetable.cellListJson)
            if (cellList.isBlank() && timetable.cellListJson.isNotBlank()) {
                allCellsConverted = false
            }

            statement.bindLong(1, timetable.createTime)
            statement.bindText(2, timetable.year)
            statement.bindText(3, timetable.semester)
            statement.bindText(4, timetable.name)
            statement.bindText(5, cellList)
            statement.step()
            statement.reset()
            statement.clearBindings()
        }
    }

    return allCellsConverted
}

private data class LegacyTimetable(
    val createTime: Long,
    val year: String,
    val semester: String,
    val name: String,
    val cellListJson: String,
)

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
    val color = legacyColorMap.getOrElse(this.color) { TimetableCellColor.entries.random() }

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
        credit = credit,
    )
}

/**
 * 레거시 JSON에만 존재하는 필드가 섞여 있어도 마이그레이션이 중단되지 않도록
 * [Json.ignoreUnknownKeys]를 켠 인스턴스로 읽는다.
 */
private val legacyCellJson = Json { ignoreUnknownKeys = true }

fun timeTableJsonDataToCellList(timeTableJsonData: String): String {
    if (timeTableJsonData.isBlank()) return ""

    return runCatching {
        val legacyCells = legacyCellJson
            .decodeFromString<List<LegacyTimeTableCell>>(timeTableJsonData)
        Json.encodeToString(legacyCells.map { it.toTimetableCell() })
    }.getOrDefault("")
}
