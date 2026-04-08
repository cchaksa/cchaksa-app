package com.chukchukhaksa.mobile.domain.graduation.repository

import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessListData

interface GraduationRepository {
  suspend fun getGraduationProgress(): GraduationProcessListData
}
