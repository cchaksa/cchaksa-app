package com.chukchukhaksa.mobile.local.database.openlecture.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chukchukhaksa.mobile.local.database.openlecture.converter.OpenLectureConverter
import com.chukchukhaksa.mobile.local.database.openlecture.dao.OpenLectureDao
import com.chukchukhaksa.mobile.local.database.openlecture.entity.OpenLectureEntity

/**
 * 버전 2에서 엔티티 구조가 통째로 바뀌었는데도 버전 번호가 그대로여서, 구버전 앱을 쓰던 기기에는
 * 옛 스키마를 담은 버전 2 데이터베이스가 남아 있다. Room 은 버전이 같으면 마이그레이션 경로를
 * 거치지 않고 `room_master_table` 의 identity hash 만 비교하며, 이 값이 어긋나면
 * `fallbackToDestructiveMigration` 과 무관하게 곧바로 예외를 던진다. 버전을 3으로 올려서 옛
 * 스키마와 새 스키마 양쪽 모두 destructive 경로를 타고 테이블이 다시 만들어지도록 한다.
 *
 * 개설 강의 목록은 서버에서 다시 받아올 수 있는 캐시라서 초기화해도 무방하다. 다만 갱신 여부를
 * 판단하는 버전 값은 Datastore 에 따로 남으므로, 테이블만 비면 목록이 빈 채로 유지된다.
 * 이를 막기 위해 `OpenLectureRepositoryImpl.checkNeedUpdate` 가 캐시가 비었는지도 함께 본다.
 */
@Database(
    entities = [OpenLectureEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(
    OpenLectureConverter::class,
)
@ConstructedBy(OpenLectureDatabaseConstructor::class)
abstract class OpenLectureDatabase : RoomDatabase() {
    abstract fun openLectureDao(): OpenLectureDao
}
