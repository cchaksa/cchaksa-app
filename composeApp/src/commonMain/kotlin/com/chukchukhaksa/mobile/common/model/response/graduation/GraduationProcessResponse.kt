package com.chukchukhaksa.mobile.common.model.response.graduation

import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcess
import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessCourse
import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessListData

data class GraduationProcessResponse(
    val data: GraduationProcessResponseData,
    val message: String,
    val success: Boolean
)

data class GraduationProcessResponseData(
  val graduationProgress: List<GraduationProgressData> = listOf(),
  val hasDifferentGraduationRequirement: Boolean = false
) {
  fun toGraduationProcessData() = GraduationProcessListData(
    graduationProgress = graduationProgress.map { it.toGraduationProcess() },
    hasDifferentGraduationRequirement = hasDifferentGraduationRequirement,
  )
}

data class GraduationProgressData(
  val areaType: String = "",
  val completedElectiveCourses: Int = 0,
  val courses: List<GraduationProcessCourseData> = listOf(),
  val earnedCredits: Int = 0,
  val requiredCredits: Int = 0,
  val requiredElectiveCourses: Int = 0,
  val totalElectiveCourses: Int = 0
) {
  fun toGraduationProcess() = GraduationProcess(
    areaType = areaType,
    completedElectiveCourses = completedElectiveCourses,
    courses = courses.map { it.toGraduationProcessCourse() },
    earnedCredits = earnedCredits,
    requiredCredits = requiredCredits,
    requiredElectiveCourses = requiredElectiveCourses,
    totalElectiveCourses = totalElectiveCourses,
  )
}

data class GraduationProcessCourseData(
  val courseName: String,
  val credits: Int,
  val grade: String,
  val semester: Int,
  val year: Int
) {
  fun toGraduationProcessCourse() = GraduationProcessCourse(
    courseName = courseName,
    credits = credits,
    grade = grade,
    semester = semester,
    year = year,
  )
}
