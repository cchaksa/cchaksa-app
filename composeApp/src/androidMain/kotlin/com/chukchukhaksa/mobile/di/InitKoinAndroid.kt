package com.chukchukhaksa.mobile.di

import android.app.Application
import com.chukchukhaksa.mobile.common.kmp.AndroidAppConfig
import com.chukchukhaksa.mobile.common.kmp.installAndroidAppConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Android 진입점. 설정 없이는 DI를 시작할 수 없도록 [AndroidAppConfig]를 필수 인자로 받는다. */
fun initKoinAndroid(application: Application, appConfig: AndroidAppConfig) {
    installAndroidAppConfig(appConfig)
    initKoin {
        androidContext(application)
        modules(module { single { appConfig } })
    }
}
