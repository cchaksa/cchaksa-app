package com.chukchukhaksa.mobile.remote.di

import com.chukchukhaksa.mobile.common.kmp.httpClientEngineFactory
import com.chukchukhaksa.mobile.common.kmp.isDebug
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.koin.dsl.module

private val prettyJson = Json { prettyPrint = true }

private fun String.prettyPrintJson(): String = try {
    val element = prettyJson.decodeFromString<JsonElement>(this)
    prettyJson.encodeToString(JsonElement.serializer(), element)
} catch (_: Exception) {
    this
}

val httpClientModule = module {
    single {
        HttpClient(httpClientEngineFactory) {
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
                        Napier.d(message, tag = "HttpClient")
                    }
                }
                level = if (isDebug) LogLevel.HEADERS else LogLevel.NONE
            }

            if (isDebug) {
                install(ResponseObserver) {
                    onResponse { response ->
                        val body = response.bodyAsText()
                        if (body.isNotBlank()) {
                            Napier.d(
                                "RESPONSE BODY:\n${body.prettyPrintJson()}",
                                tag = "HttpClient",
                            )
                        }
                    }
                }
            }

            defaultRequest {
                url("https://api.cchaksa.com/api/")
                contentType(ContentType.Application.Json)
            }
        }
    }
}
