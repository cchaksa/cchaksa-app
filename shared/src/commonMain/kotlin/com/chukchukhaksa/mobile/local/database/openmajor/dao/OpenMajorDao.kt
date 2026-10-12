package com.chukchukhaksa.mobile.local.database.openmajor.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.chukchukhaksa.mobile.local.database.openmajor.entity.OpenMajorEntity

@Dao
interface OpenMajorDao {
    @Insert
    suspend fun insertAll(lists: List<OpenMajorEntity>)

    @Query("DELETE FROM OpenMajorEntity")
    suspend fun deleteAll()

    @Query("SELECT * FROM OpenMajorEntity")
    suspend fun getAll(): List<OpenMajorEntity>
}