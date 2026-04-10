package com.chukchukhaksa.mobile.remote.portal.model

import kotlinx.serialization.Serializable

@Serializable
data class ScrapingResponseDto(
    val taskId: String? = null,
    val studentInfo: StudentInfoDto? = null,
    val status: String? = null,
)

@Serializable
data class StudentInfoDto(
    val name: String? = null,
    val school: String? = null,
    val majorName: String? = null,
    val studentCode: String? = null,
    val gradeLevel: Int? = null,
    val status: String? = null,
    val completedSemesterType: Int? = null,
)
