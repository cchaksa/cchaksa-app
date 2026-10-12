package com.chukchukhaksa.mobile.local.database.common

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun createSqliteDriver(): SQLiteDriver = BundledSQLiteDriver()
