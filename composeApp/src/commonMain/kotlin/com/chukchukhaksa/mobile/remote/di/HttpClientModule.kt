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
import org.koin.core.qualifier.named
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

// Auth 플러그인이 없는 클라이언트 식별자.
// 토큰 갱신/로그인처럼 401 자동 재발급 로직을 타면 안 되는 인증 엔드포인트 전용으로 사용한다.
internal val AUTH_REFRESH_CLIENT_QUALIFIER = named("authRefreshClient")

// AccessToken 만료 에러 코드. 이 코드일 때만 토큰 갱신을 시도한다.
internal const val ACCESS_TOKEN_EXPIRED_CODE = "T04"

// 401 응답 body에서 에러 코드를 추출한다. 파싱 실패 시 null.
private suspend fun HttpResponse.parseErrorCode(): String? = try {
    body<ApiResponse<JsonElement>>().error?.code
} catch (e: Exception) {
    null
}

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
            // AccessToken 만료(T04)일 때만 토큰 갱신을 시도한다.
            // 그 외 인증 오류(예: T11 RefreshToken 불일치)는 갱신해도 실패하므로 세션 만료로 처리한다.
            val errorCode = response.parseErrorCode()
            if (errorCode != ACCESS_TOKEN_EXPIRED_CODE) {
                localAuthDataSource.clearTokens()
                authEventBus.emit()
                return@refreshTokens null
            }

            val currentRefreshToken = localAuthDataSource.getRefreshToken()
            if (currentRefreshToken == null) {
                localAuthDataSource.clearTokens()
                authEventBus.emit()
                return@refreshTokens null
            }

            try {
                val refreshResponse = refreshClient.post("auth/refresh") {
                    setBody(RefreshRequest(refreshToken = currentRefreshToken))
                }.body<ApiResponse<RefreshResponse>>()

                val result = refreshResponse.getDataOrThrow()
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

// 디버그 빌드에서 요청/응답을 로깅하는 인터셉터를 설치한다.
private fun HttpClient.installDebugLogging(): HttpClient = also { client ->
    if (isDebug) {
        client.plugin(HttpSend).intercept { request ->
            Napier.d(buildRequestLog(request), tag = "HttpClient")
            val call = execute(request).save()
            Napier.d(buildResponseLog(call.response), tag = "HttpClient")
            call
        }
    }
}

val httpClientModule = module {
    single { AuthEventBus() }

    // Auth 플러그인이 없는 인증 전용 클라이언트.
    // 토큰 갱신(auth/refresh)·로그인(users/signin) 및 Auth 플러그인의 401 자동 재발급에 사용한다.
    // 이 클라이언트로 보낸 요청이 401을 받아도 갱신 로직이 중첩 실행되지 않는다.
    single<HttpClient>(qualifier = AUTH_REFRESH_CLIENT_QUALIFIER) {
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
            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }.installDebugLogging()
    }

    single {
        val localAuthDataSource: LocalAuthDataSource = get()
        val authEventBus: AuthEventBus = get()
        val refreshClient: HttpClient = get(qualifier = AUTH_REFRESH_CLIENT_QUALIFIER)

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
        }.installDebugLogging()
    }
}
