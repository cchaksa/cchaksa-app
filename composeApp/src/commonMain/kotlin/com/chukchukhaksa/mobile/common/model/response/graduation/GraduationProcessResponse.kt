package com.chukchukhaksa.mobile.common.model.response.graduation

import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcess
import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessCourse

data class GraduationProcessResponse(
    val data: GraduationProcessResponseData,
    val message: String,
    val success: Boolean
)

data class GraduationProcessResponseData(
  val graduationProgress: List<GraduationProgressData>,
  val hasDifferentGraduationRequirement: Boolean
)

data class GraduationProgressData(
  val areaType: String,
  val completedElectiveCourses: Int,
  val courses: List<GraduationProcessCourseData>,
  val earnedCredits: Int,
  val requiredCredits: Int,
  val requiredElectiveCourses: Int,
  val totalElectiveCourses: Int
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
