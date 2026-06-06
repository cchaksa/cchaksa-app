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
import io.ktor.client.call.save
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
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

private fun buildRequestLog(request: HttpRequestBuilder): String = buildString {
    appendLine("REQUEST: ${request.method.value} ${request.url.buildString()}")
    val headers = request.headers.entries()
    if (headers.isNotEmpty()) {
        appendLine("HEADERS")
        headers.forEach { (key, values) -> appendLine("  -> $key: ${values.joinToString()}") }
    }
    val body = request.body
    if (body is TextContent) {
        appendLine("BODY:")
        append(body.text.prettyPrintJson())
    }
}

private suspend fun buildResponseLog(response: HttpResponse): String = buildString {
    appendLine("RESPONSE: ${response.status}")
    appendLine("FROM: ${response.call.request.method.value} ${response.call.request.url}")
    val headers = response.headers.entries()
    if (headers.isNotEmpty()) {
        appendLine("HEADERS")
        headers.forEach { (key, values) -> appendLine("  -> $key: ${values.joinToString()}") }
    }
    val text = response.bodyAsText()
    if (text.isNotBlank()) {
        appendLine("BODY:")
        append(text.take(4096).prettyPrintJson())
    }
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

            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }.also { client ->
            if (isDebug) {
                client.plugin(HttpSend).intercept { request ->
                    Napier.d(buildRequestLog(request), tag = "HttpClient")
                    val call = execute(request).save()
                    Napier.d(buildResponseLog(call.response), tag = "HttpClient")
                    call
                }
            }
        }
    }
}
