package com.chukchukhaksa.mobile.presentation.home.graduationprogress

import com.chukchukhaksa.mobile.common.model.graduation.GraduationProcessCourse
import com.chukchukhaksa.mobile.common.model.response.graduation.GraduationProcessCourseData
import com.chukchukhaksa.mobile.common.model.response.graduation.GraduationProcessResponseData
import com.chukchukhaksa.mobile.common.model.response.graduation.GraduationProgressData

val graduationProgressSampleData = listOf(
  GraduationProgressData(
    areaType = "중요핵심",
    requiredCredits = 4,
    earnedCredits = 4,
    courses = listOf(
      GraduationProcessCourseData(
        courseName = "학문과사고",
        credits = 2,
        grade = "B+",
        semester = 1,
        year = 24,
      ),
      GraduationProcessCourseData(
        courseName = "도전과창조-기업가정신",
        credits = 2,
        grade = "B0",
        semester = 2,
        year = 19,
      ),
    ),
  ),
  GraduationProgressData(
    areaType = "전공기초교양",
    requiredCredits = 12,
    earnedCredits = 9,
    courses = listOf(),
  ),
  GraduationProgressData(
    areaType = "기초교양",
    requiredCredits = 12,
    earnedCredits = 12,
    courses = listOf(),
  ),
  GraduationProgressData(
    areaType = "선택교양",
    requiredCredits = 17,
    earnedCredits = 17,
    courses = listOf(),
  ),
  GraduationProgressData(
    areaType = "소양교육",
    requiredCredits = 1,
    earnedCredits = 1,
    courses = listOf(),
  ),
  GraduationProgressData(
    areaType = "전공선택",
    requiredCredits = 36,
    earnedCredits = 27,
    courses = listOf(
      GraduationProcessCourseData(
        courseName = "모바일프로그래밍",
        credits = 3,
        grade = "A+",
        semester = 1,
        year = 24,
      ),
      GraduationProcessCourseData(
        courseName = "웹크롤자료분석",
        credits = 3,
        grade = "A+",
        semester = 1,
        year = 24,
      ),
      GraduationProcessCourseData(
        courseName = "데이터구조",
        credits = 3,
        grade = "A0",
        semester = 2,
        year = 24,
      ),
      GraduationProcessCourseData(
        courseName = "정보처리기술",
        credits = 3,
        grade = "A0",
        semester = 2,
        year = 24,
      ),
      GraduationProcessCourseData(
        courseName = "공학수학1",
        credits = 3,
        grade = "B+",
        semester = 1,
        year = 19,
      ),
      GraduationProcessCourseData(
        courseName = "데이터베이스",
        credits = 3,
        grade = "B0",
        semester = 1,
        year = 24,
      ),
      GraduationProcessCourseData(
        courseName = "객체지향프로그래밍",
        credits = 3,
        grade = "C+",
        semester = 2,
        year = 19,
      ),
      GraduationProcessCourseData(
        courseName = "웹프로그래밍",
        credits = 3,
        grade = "C0",
        semester = 1,
        year = 19,
      ),
      GraduationProcessCourseData(
        courseName = "디지털논리설계",
        credits = 3,
        grade = "D0",
        semester = 1,
        year = 19,
      ),
    ),
  ),
  GraduationProgressData(
    areaType = "진로취업",
    requiredCredits = 3,
    earnedCredits = 3,
    courses = listOf(),
  ),
  GraduationProgressData(
    areaType = "일반선택",
    requiredCredits = 24,
    earnedCredits = 13,
    courses = listOf(),
  ),
)

val graduationProgressResponseSampleData = GraduationProcessResponseData(
  graduationProgress = graduationProgressSampleData,
  hasDifferentGraduationRequirement = true,
)
