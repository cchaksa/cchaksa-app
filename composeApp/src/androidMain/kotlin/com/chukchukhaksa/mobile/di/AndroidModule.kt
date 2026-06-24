package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.BuildConfig
import com.chukchukhaksa.mobile.common.ad.AdManager
import com.chukchukhaksa.mobile.common.ad.AndroidAdManager
import com.chukchukhaksa.mobile.common.analytics.AnalyticsClient
import com.chukchukhaksa.mobile.common.analytics.AndroidAnalyticsClient
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewHolder
import com.chukchukhaksa.mobile.common.kmp.AndroidAppLifecycleObserver
import com.chukchukhaksa.mobile.common.kmp.AppLifecycleObserver
import com.chukchukhaksa.mobile.common.kmp.AppleSignInClient
import com.chukchukhaksa.mobile.common.kmp.KakaoSignInClient
import com.chukchukhaksa.mobile.common.kmp.isDebug
import eu.anifantakis.lib.ksafe.KSafe
import com.chukchukhaksa.mobile.local.database.openlecture.database.OpenLectureDatabaseFactory
import com.chukchukhaksa.mobile.local.database.openmajor.database.OpenMajorDatabaseFactory
import com.chukchukhaksa.mobile.local.database.timetable.database.TimetableDatabaseFactory
import com.chukchukhaksa.mobile.local.datastore.ChukChukHaksaDataStoreFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

actual val platformModule = module {
    single { TimetableDatabaseFactory(androidApplication()) }
    single { OpenMajorDatabaseFactory(androidApplication()) }
    single { OpenLectureDatabaseFactory(androidApplication()) }
    single { ChukChukHaksaDataStoreFactory(androidApplication()) }
    factory { AppleSignInClient() }
    factory { KakaoSignInClient() }
    single { KSafe(androidApplication()) }
    single { WebViewHolder(androidApplication()) }
    single<AdManager> { AndroidAdManager(androidApplication()) }
    single<AppLifecycleObserver> { AndroidAppLifecycleObserver(androidApplication()) }
    single<AnalyticsClient> {
        AndroidAnalyticsClient(
            androidApplication(),
            if (isDebug) BuildConfig.AMPLITUDE_API_KEY_DEV else BuildConfig.AMPLITUDE_API_KEY_PROD,
        )
    }
}
