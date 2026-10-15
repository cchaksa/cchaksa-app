package com.chukchukhaksa.mobile.data.timetable.repository

import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.data.timetable.datasource.LocalTimetableDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimetableRepositoryImplTest {

    private class FakeLocalTimetableDataSource(
        timetables: List<Timetable> = emptyList(),
        mainCreateTime: Long? = null,
    ) : LocalTimetableDataSource {
        val timetables = timetables.toMutableList()
        val mainCreateTime = MutableStateFlow(mainCreateTime)
        private val cellType = MutableStateFlow("")

        override suspend fun getAllTimetable(): List<Timetable> = timetables.toList()

        override suspend fun getTimetable(createTime: Long): Timetable? =
            timetables.firstOrNull { it.createTime == createTime }

        override suspend fun setMainTimetableCreateTime(createTime: Long) {
            mainCreateTime.value = createTime
        }

        override suspend fun getMainTimetableCreateTime(): Flow<Long?> = mainCreateTime

        override suspend fun deleteAllTimetable() {
            timetables.clear()
        }

        override suspend fun deleteTimetable(data: Timetable) {
            timetables.removeAll { it.createTime == data.createTime }
        }

        override suspend fun updateTimetable(data: Timetable) {
            val index = timetables.indexOfFirst { it.createTime == data.createTime }
            if (index >= 0) timetables[index] = data
        }

        override suspend fun insertTimetable(data: Timetable) {
            timetables += data
        }

        override suspend fun getTimetableCellType(): Flow<String> = cellType

        override suspend fun setTimetableCellType(type: String) {
            cellType.value = type
        }
    }

    private val first = Timetable(createTime = 1L, name = "first")
    private val second = Timetable(createTime = 2L, name = "second")

    @Test
    fun deletingMainTimetableMakesFirstRemainingTimetableMain() = runTest {
        val dataSource = FakeLocalTimetableDataSource(listOf(first, second), mainCreateTime = first.createTime)
        val repository = TimetableRepositoryImpl(dataSource)

        repository.deleteTimetable(first)

        assertEquals(listOf(second), dataSource.timetables)
        assertEquals(second.createTime, dataSource.mainCreateTime.value)
    }

    @Test
    fun deletingNonMainTimetableKeepsMain() = runTest {
        val dataSource = FakeLocalTimetableDataSource(listOf(first, second), mainCreateTime = second.createTime)
        val repository = TimetableRepositoryImpl(dataSource)

        repository.deleteTimetable(first)

        assertEquals(second.createTime, dataSource.mainCreateTime.value)
    }

    @Test
    fun insertTimetableCreatesEmptyTimetableAndMakesItMain() = runTest {
        val dataSource = FakeLocalTimetableDataSource(listOf(first), mainCreateTime = first.createTime)
        val repository = TimetableRepositoryImpl(dataSource)

        repository.insertTimetable(year = "2026", semester = "2", name = "new")

        val inserted = dataSource.timetables.last()
        assertEquals("2026", inserted.year)
        assertEquals("2", inserted.semester)
        assertEquals("new", inserted.name)
        assertTrue(inserted.cellList.isEmpty())
        assertEquals(inserted.createTime, dataSource.mainCreateTime.value)
    }

    @Test
    fun getMainTimetableMakesFirstTimetableMainWhenNoneIsStored() = runTest {
        val dataSource = FakeLocalTimetableDataSource(listOf(first, second), mainCreateTime = null)
        val repository = TimetableRepositoryImpl(dataSource)

        val main = repository.getMainTimetable()

        assertEquals(first, main)
        assertEquals(first.createTime, dataSource.mainCreateTime.value)
    }
}
