package com.chukchukhaksa.mobile.common.model.academic

data class AcademicRecord(
  val courses: AcademicRecordCourses,
  val semesterGrade: SemesterGrade
)

data class AcademicRecordCourses(
  val liberal: List<Liberal>,
  val major: List<Major>
)

data class SemesterGrade(
  val attemptedCredits: Int,
  val classRank: Int,
  val earnedCredits: Int,
  val percentile: Double,
  val semester: Int,
  val semesterGpa: Double,
  val totalStudents: Int,
  val year: Int
)

data class Major(
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
)

data class Liberal(
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
)
