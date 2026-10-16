package com.chukchukhaksa.mobile.data.timetable.repository

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.model.TimetableCell
import kotlinx.coroutines.CompletableDeferred

/**
 * presenter 테스트용 [TimetableRepository]. 시간표 생성·수정 호출을 기록하고,
 * [failure]를 지정하면 그 예외를 던지고, [gate]를 지정하면 그것이 완료될 때까지 생성·수정을 멈춰 둔다.
 * 나머지 기능은 쓰는 테스트가 생길 때 채운다.
 */
class FakeTimetableRepository(
    var failure: Throwable? = null,
    var gate: CompletableDeferred<Unit>? = null,
) : TimetableRepository {
    data class Upsert(val createTime: Long?, val year: String, val semester: String, val name: String)

    val inserted = mutableListOf<Upsert>()
    val updated = mutableListOf<Upsert>()

    override suspend fun insertTimetable(year: String, semester: String, name: String) {
        gate?.await()
        failure?.let { throw it }
        inserted += Upsert(createTime = null, year = year, semester = semester, name = name)
    }

    override suspend fun updateTimetable(createTime: Long, year: String, semester: String, name: String) {
        gate?.await()
        failure?.let { throw it }
        updated += Upsert(createTime = createTime, year = year, semester = semester, name = name)
    }

    override suspend fun getAllTimetable(): List<Timetable> = TODO()
    override suspend fun getMainTimetable(): Timetable? = TODO()
    override suspend fun setMainTimetableCreateTime(createTime: Long): Unit = TODO()
    override suspend fun getMainTimetableCreateTime(): Long? = TODO()
    override suspend fun deleteTimetable(data: Timetable): Unit = TODO()
    override suspend fun insertTimetableCell(cellList: List<TimetableCell>): Unit = TODO()
    override suspend fun deleteTimetableCell(cell: TimetableCell): Timetable = TODO()
    override suspend fun updateTimetableCell(oldCellId: String, cellList: List<TimetableCell>): Unit = TODO()
    override suspend fun getTimetableCellType(): String = TODO()
    override suspend fun setTimetableCellType(type: String): Unit = TODO()
}
