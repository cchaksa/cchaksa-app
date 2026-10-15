package com.chukchukhaksa.mobile.data.timetable.repository

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell

interface TimetableRepository {
    suspend fun getAllTimetable(): List<Timetable>

    /** 메인 시간표를 돌려준다. 메인으로 지정된 시간표가 없으면 첫 번째 시간표를 메인으로 지정한다. */
    suspend fun getMainTimetable(): Timetable?

    suspend fun setMainTimetableCreateTime(createTime: Long)

    suspend fun getMainTimetableCreateTime(): Long?

    /** 시간표를 지운다. 지운 시간표가 메인이었다면 남은 첫 번째 시간표를 메인으로 지정한다. */
    suspend fun deleteTimetable(data: Timetable)

    suspend fun updateTimetable(
        createTime: Long,
        year: String,
        semester: String,
        name: String,
    )

    /** 빈 시간표를 새로 만들고 메인 시간표로 지정한다. */
    suspend fun insertTimetable(
        year: String,
        semester: String,
        name: String,
    )

    suspend fun insertTimetableCell(cellList: List<TimetableCell>)

    suspend fun deleteTimetableCell(cell: TimetableCell): Timetable

    suspend fun updateTimetableCell(oldCellId: String, cellList: List<TimetableCell>)

    suspend fun getTimetableCellType(): String
    suspend fun setTimetableCellType(type: String)
}
