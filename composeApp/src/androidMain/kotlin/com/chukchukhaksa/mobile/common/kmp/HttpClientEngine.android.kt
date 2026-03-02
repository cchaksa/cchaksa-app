package com.chukchukhaksa.mobile.common.kmp

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual val httpClientEngineFactory: HttpClientEngineFactory<*> = OkHttp
