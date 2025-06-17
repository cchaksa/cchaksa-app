package com.chukchukhaksa.mobile.data.openlecture.repository

import com.chukchukhaksa.mobile.common.model.OpenLecture
import com.chukchukhaksa.mobile.domain.timetable.repository.OpenLectureRepository
import com.chukchukhaksa.mobile.data.openlecture.datasource.LocalOpenLectureDataSource
import com.chukchukhaksa.mobile.data.openlecture.datasource.RemoteOpenLectureDataSource
import com.chukchukhaksa.mobile.data.timetable.TimetableUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime

class OpenLectureRepositoryImpl(
    private val remoteOpenLectureDataSource: RemoteOpenLectureDataSource,
    private val localOpenLectureDataSource: LocalOpenLectureDataSource,
) : OpenLectureRepository {
    override suspend fun getOpenLectureList(
        lectureOrProfessorName: String?,
        major: String?,
        grade: Int?,
    ): List<OpenLecture> {
        return localOpenLectureDataSource.getOpenLectureList(
            lectureOrProfessorName = lectureOrProfessorName,
            major = major,
            grade = grade,
        )
    }

    override suspend fun checkNeedUpdate(): Boolean {
        val localVersion =
            localOpenLectureDataSource.getOpenLectureListVersion().firstOrNull() ?: return true
        val remoteVersion = remoteOpenLectureDataSource.getOpenLectureListVersion()
        return remoteVersion > localVersion
    }

    override suspend fun updateAllLectures() = coroutineScope {
        val remoteOpenLectures = async {
            remoteOpenLectureDataSource.getOpenLectureList().map {
                OpenLecture(
                    id = it.number,
                    name = it.className,
                    type = it.classification,
                    major = it.major,
                    grade = it.grade,
                    professorName = it.professor,
                    originalCellList = TimetableUtil.parseTimeTableString(it.time),
                )
            }
        }

        val remoteOpenLectureVersion = async {
            remoteOpenLectureDataSource.getOpenLectureListVersion()
        }

        localOpenLectureDataSource.updateAllLectures(remoteOpenLectures.await())
        localOpenLectureDataSource.setOpenLectureListVersion(remoteOpenLectureVersion.await())
    }

    override suspend fun getLastUpdatedDate(): String? {
        return try {
            val version =
                localOpenLectureDataSource.getOpenLectureListVersion().firstOrNull()?.toString()
            if (version == null || version.length != 12) return null

            val year = version.substring(0, 4).toInt()
            val month = version.substring(4, 6).toInt()
            val day = version.substring(6, 8).toInt()
            val hour = version.substring(8, 10).toInt()
            val minute = version.substring(10, 12).toInt()

            val dateTime = LocalDateTime(year, month, day, hour, minute)

            "${dateTime.year}년 ${dateTime.monthNumber}월 ${dateTime.dayOfMonth}일 " +
                    "${dateTime.hour}시 ${dateTime.minute}분"
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getOpenMajor(): List<String> {
        return localOpenLectureDataSource.getOpenLectureList().map { list -> list.major }
    }
}
