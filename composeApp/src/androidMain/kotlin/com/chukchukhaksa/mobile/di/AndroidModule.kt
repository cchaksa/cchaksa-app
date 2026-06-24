package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.BuildConfig
import com.chukchukhaksa.mobile.common.ad.AdManager
import com.chukchukhaksa.mobile.common.ad.AndroidAdManager
import com.chukchukhaksa.mobile.common.analytics.AnalyticsClient
import com.chukchukhaksa.mobile.common.analytics.AndroidAnalyticsClient
import com.chukchukhaksa.mobile.common.designsystem.component.webview.WebViewHolder
import com.chukchukhaksa.mobile.common.kmp.AdvertisingIdProvider
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
    factory { AdvertisingIdProvider() }
    single { KSafe(androidApplication()) }
    single { WebViewHolder(androidApplication()) }
    // createdAtStart: Koin 시작(=Application.onCreate) 시점에 즉시 생성해야
    // AndroidAdManager가 ActivityLifecycleCallbacks를 MainActivity.onResume보다 먼저 등록한다.
    // 지연 생성(첫 광고 게이트 진입 시 해석)하면 이미 지나간 onResume 콜백을 놓쳐
    // 표시 시점에 포그라운드 Activity를 못 찾고 NotReady로 실패한다.
    single<AdManager>(createdAtStart = true) { AndroidAdManager(androidApplication()) }
    single<AppLifecycleObserver> { AndroidAppLifecycleObserver(androidApplication()) }
    single<AnalyticsClient> {
        AndroidAnalyticsClient(
            androidApplication(),
            if (isDebug) BuildConfig.AMPLITUDE_API_KEY_DEV else BuildConfig.AMPLITUDE_API_KEY_PROD,
        )
    }
}
