package com.chukchukhaksa.mobile.data.openmajor.repository

import kotlinx.coroutines.flow.Flow

interface OpenMajorRepository {
    suspend fun getOpenMajorList(): Flow<List<String>>
}