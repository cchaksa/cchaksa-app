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
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.koin.dsl.module

private val prettyJson = Json { prettyPrint = true }

private fun prettyPrintIfJson(message: String): String {
    return try {
        val element = prettyJson.decodeFromString<JsonElement>(message)
        prettyJson.encodeToString(JsonElement.serializer(), element)
    } catch (_: Exception) {
        message
    }
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
                        Napier.d(prettyPrintIfJson(message), tag = "HttpClient")
                    }
                }
                level = if (isDebug) LogLevel.ALL else LogLevel.NONE
            }

            defaultRequest {
                url("https://api.cchaksa.com/api/")
                contentType(ContentType.Application.Json)
            }
        }
    }
}
