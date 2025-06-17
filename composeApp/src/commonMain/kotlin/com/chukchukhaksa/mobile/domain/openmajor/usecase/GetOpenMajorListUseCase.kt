package com.chukchukhaksa.mobile.domain.openmajor.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetOpenMajorListUseCase {
    operator fun invoke(): Flow<List<String>> = flow {
        emit(listOf("전체", "컴퓨터SW"))
    }
}