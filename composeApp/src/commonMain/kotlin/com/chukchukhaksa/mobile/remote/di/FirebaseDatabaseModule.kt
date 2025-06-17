package com.chukchukhaksa.mobile.remote.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import org.koin.dsl.module

val firebaseDatabaseModule =
    module {
        single {
            Firebase
                .database("https://chukchuk-haksa-default-rtdb.asia-southeast1.firebasedatabase.app")
                .apply {
                    setLoggingEnabled(true)
                }
        }
    }