package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewHolder
import com.chukchukhaksa.mobile.common.kmp.AppleSignInClient
import com.chukchukhaksa.mobile.common.kmp.KakaoSignInClient
import eu.anifantakis.lib.ksafe.KSafe
import com.chukchukhaksa.mobile.local.database.openlecture.database.OpenLectureDatabaseFactory
import com.chukchukhaksa.mobile.local.database.openmajor.database.OpenMajorDatabaseFactory
import com.chukchukhaksa.mobile.local.database.timetable.database.TimetableDatabaseFactory
import com.chukchukhaksa.mobile.local.datastore.ChukChukHaksaDataStoreFactory
import org.koin.dsl.module

actual val platformModule
    get() = module {
        single { TimetableDatabaseFactory() }
        single { OpenMajorDatabaseFactory() }
        single { OpenLectureDatabaseFactory() }
        single { ChukChukHaksaDataStoreFactory() }
        factory { AppleSignInClient() }
        factory { KakaoSignInClient(get()) }
        single { KSafe() }
        single { WebViewHolder() }
    }