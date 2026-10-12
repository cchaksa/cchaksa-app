package com.chukchukhaksa.mobile.local.database.common.di

import com.chukchukhaksa.mobile.local.database.common.createSqliteDriver
import com.chukchukhaksa.mobile.local.database.openlecture.database.OpenLectureDatabase
import com.chukchukhaksa.mobile.local.database.openlecture.database.OpenLectureDatabaseFactory
import com.chukchukhaksa.mobile.local.database.openmajor.database.OpenMajorDatabase
import com.chukchukhaksa.mobile.local.database.openmajor.database.OpenMajorDatabaseFactory
import com.chukchukhaksa.mobile.local.database.timetable.database.TimetableDatabase
import com.chukchukhaksa.mobile.local.database.timetable.database.TimetableDatabaseFactory
import com.chukchukhaksa.mobile.local.database.timetable.migration.TIMETABLE_MIGRATION_1_2
import org.koin.dsl.module

val databaseModule =
  module {
    includes(
      timetableDatabaseModule,
      openMajorDatabaseModule,
      openLectureDatabaseModule,
    )
  }

/**
 * 시간표는 다시 받아올 수 없는 데이터지만, 마이그레이션 경로가 없을 때 예외를 던지면 그 사용자는
 * 앱을 아예 쓸 수 없게 된다. 시간표를 잃는 쪽이 앱 자체를 못 쓰는 쪽보다 낫다고 보아 테이블
 * 초기화를 허용한다. 다만 이 설정은 마이그레이션 누락을 런타임에 조용히 덮어 주므로, 엔티티를
 * 고칠 때 마이그레이션을 함께 등록하는 일은 온전히 사람이 챙겨야 한다.
 */
val timetableDatabaseModule = module {
  single {
    get<TimetableDatabaseFactory>()
      .create()
      .addMigrations(TIMETABLE_MIGRATION_1_2)
      .fallbackToDestructiveMigration(true)
      .setDriver(createSqliteDriver())
      .build()
  }
  single { get<TimetableDatabase>().timetableDao() }
}

val openMajorDatabaseModule = module {
  single {
    get<OpenMajorDatabaseFactory>()
      .create()
      .fallbackToDestructiveMigration(true)
      .setDriver(createSqliteDriver())
      .build()
  }
  single { get<OpenMajorDatabase>().openMajorDao() }
}

val openLectureDatabaseModule = module {
  single {
    get<OpenLectureDatabaseFactory>()
      .create()
      .fallbackToDestructiveMigration(true)
      .setDriver(createSqliteDriver())
      .build()
  }
  single { get<OpenLectureDatabase>().openLectureDao() }
}
