package com.chukchukhaksa.mobile.domain.openmajor.repository

import kotlinx.coroutines.flow.Flow

interface OpenMajorRepository {
    suspend fun getOpenMajorList(): Flow<List<String>>
}