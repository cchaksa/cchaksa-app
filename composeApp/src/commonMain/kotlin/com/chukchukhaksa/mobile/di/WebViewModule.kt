package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.common.designsystem.component.webview.sessionApiBaseUrl
import com.chukchukhaksa.mobile.common.kmp.httpClientEngineFactory
import com.chukchukhaksa.mobile.common.kmp.isDebug
import com.chukchukhaksa.mobile.domain.webview.ExchangeWebSessionUseCase
import com.chukchukhaksa.mobile.domain.webview.WebViewPreloader
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.WebViewGuideViewModel
import com.chukchukhaksa.mobile.remote.auth.SessionApi
import com.chukchukhaksa.mobile.remote.auth.SessionApiImpl
import org.koin.core.module.dsl.viewModelOf
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val SESSION_EXCHANGE_QUALIFIER = named("sessionExchange")

val webViewModule = module {
  single<HttpClient>(qualifier = SESSION_EXCHANGE_QUALIFIER) {
    HttpClient(httpClientEngineFactory) {
      install(HttpTimeout) {
        requestTimeoutMillis = 10_000
        connectTimeoutMillis = 5_000
        socketTimeoutMillis = 10_000
      }
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            isLenient = true
          },
        )
      }
      install(Logging) {
        logger = object : Logger {
          override fun log(message: String) {
            Napier.d(message, tag = "SessionApi")
          }
        }
        level = if (isDebug) LogLevel.HEADERS else LogLevel.NONE
      }
      defaultRequest {
        contentType(ContentType.Application.Json)
      }
    }
  }

  single<SessionApi> {
    SessionApiImpl(
      client = get(qualifier = SESSION_EXCHANGE_QUALIFIER),
      baseUrl = sessionApiBaseUrl,
    )
  }

  single { ExchangeWebSessionUseCase(localAuthDataSource = get(), sessionApi = get()) }

  single { WebViewPreloader(exchangeWebSession = get(), webViewHolder = get()) }

  viewModelOf(::WebViewGuideViewModel)
}
