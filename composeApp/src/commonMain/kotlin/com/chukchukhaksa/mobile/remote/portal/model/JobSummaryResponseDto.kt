package com.chukchukhaksa.mobile.remote.portal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobSummaryResponseDto(
    @SerialName("job_id") val jobId: String? = null,
    val studentInfo: StudentInfoDto? = null,
    @SerialName("finished_at") val finishedAt: String? = null,
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
