package com.chukchukhaksa.mobile.common.model.response.academic

import com.chukchukhaksa.mobile.common.model.academic.AcademicRecord
import com.chukchukhaksa.mobile.common.model.academic.AcademicRecordCourses
import com.chukchukhaksa.mobile.common.model.academic.Liberal
import com.chukchukhaksa.mobile.common.model.academic.Major
import com.chukchukhaksa.mobile.common.model.academic.SemesterGrade

data class AcademicRecordResponse(
    val data: AcademicRecordResponseData,
    val message: String,
    val success: Boolean
)

data class AcademicRecordResponseData(
  val courses: AcademicRecordCoursesData,
  val semesterGrade: SemesterGradeData
) {
  fun toAcademicRecord() = AcademicRecord(
    courses = courses.toAcademicRecordCourses(),
    semesterGrade = semesterGrade.toSemesterGrade()
  )
}

data class AcademicRecordCoursesData(
  val liberal: List<LiberalData>,
  val major: List<MajorData>
) {
  fun toAcademicRecordCourses() = AcademicRecordCourses(
    liberal = liberal.map { it.toLiberal() },
    major = major.map { it.toMajor() }
  )
}


data class SemesterGradeData(
  val attemptedCredits: Int,
  val classRank: Int,
  val earnedCredits: Int,
  val percentile: Double,
  val semester: Int,
  val semesterGpa: Double,
  val totalStudents: Int,
  val year: Int
) {
  fun toSemesterGrade() = SemesterGrade(
    attemptedCredits = attemptedCredits,
    classRank = classRank,
    earnedCredits = earnedCredits,
    percentile = percentile,
    semester = semester,
    semesterGpa = semesterGpa,
    totalStudents = totalStudents,
    year = year,
  )
}

data class MajorData(
  val areaType: String,
  val courseCode: String,
  val courseName: String,
  val credits: Int,
  val grade: String,
  val id: String,
  val isOnline: Boolean,
  val isRetake: Boolean,
  val isRetakeDelete: Boolean,
  val originalScore: Int,
  val professor: String,
  val score: Int,
  val semester: Int,
  val year: Int
) {
  fun toMajor() = Major(
    areaType = areaType,
    courseCode = courseCode,
    courseName = courseName,
    credits = credits,
    grade = grade,
    id = id,
    isOnline = isOnline,
    isRetake = isRetake,
    isRetakeDelete = isRetakeDelete,
    originalScore = originalScore,
    professor = professor,
    score = score,
    semester = semester,
    year = year,
  )
}

data class LiberalData(
  val areaType: String,
  val courseCode: String,
  val courseName: String,
  val credits: Int,
  val grade: String,
  val id: String,
  val isOnline: Boolean,
  val isRetake: Boolean,
  val isRetakeDelete: Boolean,
  val originalScore: Int,
  val professor: String,
  val score: Int,
  val semester: Int,
  val year: Int
) {
  fun toLiberal() = Liberal(
    areaType = areaType,
    courseCode = courseCode,
    courseName = courseName,
    credits = credits,
    grade = grade,
    id = id,
    isOnline = isOnline,
    isRetake = isRetake,
    isRetakeDelete = isRetakeDelete,
    originalScore = originalScore,
    professor = professor,
    score = score,
    semester = semester,
    year = year,
  )
}
