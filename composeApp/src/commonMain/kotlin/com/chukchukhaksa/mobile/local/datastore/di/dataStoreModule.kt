package com.chukchukhaksa.mobile.local.datastore.di

import com.chukchukhaksa.mobile.local.datastore.ChukChukHaksaDataStoreFactory
import org.koin.dsl.module

val dataStoreModule =
    module {
        single { get<ChukChukHaksaDataStoreFactory>().createChukChukHaksaDataStore() }
    }