package com.chukchukhaksa.mobile.remote.di

import com.chukchukhaksa.mobile.common.kmp.httpClientEngineFactory
import com.chukchukhaksa.mobile.common.kmp.isDebug
import com.chukchukhaksa.mobile.data.auth.datasource.LocalAuthDataSource
import com.chukchukhaksa.mobile.remote.auth.AuthEventBus
import com.chukchukhaksa.mobile.remote.auth.model.RefreshRequest
import com.chukchukhaksa.mobile.remote.auth.model.RefreshResponse
import com.chukchukhaksa.mobile.remote.common.ApiResponse
import com.chukchukhaksa.mobile.remote.common.getDataOrThrow
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.plugins.plugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
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

private val BASE_URL = if (isDebug) "https://dev.api.cchaksa.com/api/" else "https://api.cchaksa.com/api/"

internal val AUTH_EXCLUDED_PATHS = listOf(
    "auth/refresh",
    "users/signin",
)

internal fun AuthConfig.configureBearerAuth(
    localAuthDataSource: LocalAuthDataSource,
    authEventBus: AuthEventBus,
    refreshClient: HttpClient,
) {
    bearer {
        loadTokens {
            val accessToken = localAuthDataSource.getAccessToken()
            val refreshToken = localAuthDataSource.getRefreshToken()
            if (accessToken != null && refreshToken != null) {
                BearerTokens(accessToken, refreshToken)
            } else {
                null
            }
        }

        refreshTokens {
            val currentRefreshToken = localAuthDataSource.getRefreshToken()
            if (currentRefreshToken == null) {
                localAuthDataSource.clearTokens()
                authEventBus.emit()
                return@refreshTokens null
            }

            try {
                val response = refreshClient.post("auth/refresh") {
                    setBody(RefreshRequest(refreshToken = currentRefreshToken))
                }.body<ApiResponse<RefreshResponse>>()

                val result = response.getDataOrThrow()
                localAuthDataSource.saveAccessToken(result.accessToken)
                localAuthDataSource.saveRefreshToken(result.refreshToken)
                BearerTokens(result.accessToken, result.refreshToken)
            } catch (e: Exception) {
                Napier.e("Token refresh failed", e)
                localAuthDataSource.clearTokens()
                authEventBus.emit()
                null
            }
        }

        sendWithoutRequest { request ->
            val requestPath = request.url.pathSegments.joinToString("/")
            AUTH_EXCLUDED_PATHS.none { path ->
                requestPath.contains(path)
            }
        }
    }
}

val httpClientModule = module {
    single { AuthEventBus() }

    single {
        val localAuthDataSource: LocalAuthDataSource = get()
        val authEventBus: AuthEventBus = get()

        val refreshClient = HttpClient(httpClientEngineFactory) {
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
            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }

        HttpClient(httpClientEngineFactory) {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }

            install(Auth) {
                configureBearerAuth(localAuthDataSource, authEventBus, refreshClient)
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(message, tag = "HttpClient")
                    }
                }
                level = if (isDebug) LogLevel.HEADERS else LogLevel.NONE
                sanitizeHeader { header ->
                    header.equals(HttpHeaders.Authorization, ignoreCase = true) ||
                        header.equals(HttpHeaders.Cookie, ignoreCase = true) ||
                        header.equals(HttpHeaders.SetCookie, ignoreCase = true)
                }
            }

            if (isDebug) {
                install(ResponseObserver) {
                    onResponse { response ->
                        val contentType = response.headers[HttpHeaders.ContentType]
                        if (contentType?.contains("application/json", ignoreCase = true) != true) return@onResponse

                        val body = response.bodyAsText()
                        if (body.isNotBlank()) {
                            val preview = body.take(4096)
                            Napier.d(
                                "RESPONSE BODY:\n${preview.prettyPrintJson()}",
                                tag = "HttpClient",
                            )
                        }
                    }
                }
            }

            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }.also { client ->
            if (isDebug) {
                client.plugin(HttpSend).intercept { request ->
                    val body = request.body
                    if (body is TextContent) {
                        Napier.d(
                            "REQUEST BODY:\n${body.text.prettyPrintJson()}",
                            tag = "HttpClient",
                        )
                    }
                    execute(request)
                }
            }
        }
    }
}
