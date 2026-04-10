package com.chukchukhaksa.mobile.domain.portal.model

data class ScrapingResult(
    val jobId: String?,
    val studentInfo: StudentInfo?,
    val status: String?,
    val finishedAt: String?,
)

data class StudentInfo(
    val name: String?,
    val school: String?,
    val majorName: String?,
    val studentCode: String?,
    val gradeLevel: Int?,
    val status: String?,
    val completedSemesterType: Int?,
)
