package com.chukchukhaksa.mobile.local.database.openmajor.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.chukchukhaksa.mobile.local.database.openmajor.dao.OpenMajorDao
import com.chukchukhaksa.mobile.local.database.openmajor.entity.OpenMajorEntity

/**
 * 1에서 2로 올라갈 때 스키마는 바뀌지 않았다. 두 버전의 `identityHash`가 같고, 저장소의 모든
 * 이력에서 테이블 이름도 줄곧 `OpenMajorEntity`였다. 이전에는 `OpenMajorData`에서 이름을
 * 바꾼다고 선언한 `AutoMigrationSpec`이 붙어 있었으나, 그런 이름의 테이블은 존재한 적이 없어
 * 선언을 제거했다.
 */
@Database(
    entities = [OpenMajorEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
    exportSchema = true,
)
@ConstructedBy(OpenMajorDatabaseConstructor::class)
abstract class OpenMajorDatabase : RoomDatabase() {
    abstract fun openMajorDao(): OpenMajorDao
}
