package com.chukchukhaksa.mobile.remote.timetable

import com.chukchukhaksa.mobile.data.openlecture.OpenLectureRaw
import com.chukchukhaksa.mobile.data.openlecture.datasource.RemoteOpenLectureDataSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.database.database
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class RemoteOpenLectureDataSourceImpl(
    private val firebaseDatabase: FirebaseDatabase,
) : RemoteOpenLectureDataSource {

    override suspend fun getOpenLectureListVersion(): Long =
        firebaseDatabase
            .reference(DATABASE_OPEN_LECTURE_VERSION)
            .valueEvents
            .first()
            .value
            .toString()
            .toLongOrNull() ?: 0

    override suspend fun getOpenLectureList(): List<OpenLectureRaw> =
        firebaseDatabase
            .reference(DATABASE_OPEN_LECTURE)
            .valueEvents
            .first()
            .children
            .mapIndexed { index, dataSnapshot ->
                val data = dataSnapshot.value as Map<*, *>
                OpenLectureRaw(
                    number = index.toLong() + 1,
                    major = data[FIELD_MAJOR].toString(),
                    grade = data[FIELD_GRADE].toString().toIntOrNull() ?: 1,
                    className = data[FIELD_CLASS_NAME].toString(),
                    classification = data[FIELD_CLASSIFICATION].toString(),
                    professor = data[FIELD_PROFESSOR]?.toString() ?: DEFAULT,
                    time = data[FIELD_TIME]?.toString() ?: DEFAULT,
                )
            }


    companion object {
        private const val FIELD_MAJOR = "m"
        private const val FIELD_GRADE = "g"
        private const val FIELD_CLASS_NAME = "n"
        private const val FIELD_CLASSIFICATION = "d"
        private const val FIELD_PROFESSOR = "p"
        private const val FIELD_TIME = "t"

        private const val DEFAULT = "없음"

        private const val DATABASE_OPEN_LECTURE = "openLecture"
        private const val DATABASE_OPEN_LECTURE_VERSION = "openLectureVersion"
    }
}
