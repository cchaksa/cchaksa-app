package com.chukchukhaksa.mobile.local.database.common

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.AndroidSQLiteDriver

actual fun createSqliteDriver(): SQLiteDriver = AndroidSQLiteDriver()
