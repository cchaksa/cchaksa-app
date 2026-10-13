package com.chukchukhaksa.mobile.common.analytics.di

import com.chukchukhaksa.mobile.common.analytics.AnalyticsUserSync
import org.koin.dsl.module

val analyticsModule = module {
    factory { AnalyticsUserSync(get(), get(), get()) }
}
