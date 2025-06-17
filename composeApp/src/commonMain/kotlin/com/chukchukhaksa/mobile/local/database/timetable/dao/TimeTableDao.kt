package com.chukchukhaksa.mobile.local.database.timetable.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.chukchukhaksa.mobile.local.database.timetable.entity.TimetableEntity

@Dao
interface TimeTableDao {
    @Query("SELECT * FROM TimetableEntity ORDER BY createTime DESC")
    suspend fun getAll(): List<TimetableEntity>

    @Query("SELECT * FROM TimetableEntity WHERE createTime = :createTime")
    suspend fun get(createTime: Long): TimetableEntity?

    @Insert
    suspend fun insert(data: TimetableEntity)

    @Query("DELETE FROM TimetableEntity")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(data: TimetableEntity)

    @Update
    suspend fun update(data: TimetableEntity)
}
