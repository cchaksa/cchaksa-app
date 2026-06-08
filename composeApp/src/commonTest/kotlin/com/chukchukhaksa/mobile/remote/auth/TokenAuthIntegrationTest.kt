package com.chukchukhaksa.mobile.remote.auth

import com.chukchukhaksa.mobile.remote.di.configureBearerAuth
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TokenAuthIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    // --- JSON Response Helpers ---

    private fun MockRequestHandleScope.jsonResponse(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(
        content = body,
        status = status,
        headers = jsonHeaders,
    )

    private fun successApiResponse(): String =
        """{"success":true,"data":"ok"}"""

    private fun errorApiResponse(
        code: String = "T04",
        message: String = "만료된 토큰입니다.",
    ): String =
        """{"success":false,"data":null,"error":{"code":"$code","message":"$message"}}"""

    private fun refreshSuccessResponse(
        accessToken: String = "new-access-token",
        refreshToken: String = "new-refresh-token",
    ): String =
        """{"success":true,"data":{"accessToken":"$accessToken","refreshToken":"$refreshToken"}}"""

    // --- Test HttpClient Factory ---

    private fun buildTestClient(
        fakeDataSource: FakeLocalAuthDataSource,
        authEventBus: AuthEventBus,
        mainEngine: MockEngine,
        refreshEngine: MockEngine,
    ): HttpClient {
        val refreshClient = HttpClient(refreshEngine) {
            install(ContentNegotiation) { json(json) }
            defaultRequest {
                url("https://api.test.com/api/")
                contentType(ContentType.Application.Json)
            }
        }

        return HttpClient(mainEngine) {
            install(ContentNegotiation) { json(json) }

            install(Auth) {
                configureBearerAuth(fakeDataSource, authEventBus, refreshClient)
            }

            defaultRequest {
                url("https://api.test.com/api/")
                contentType(ContentType.Application.Json)
            }
        }
    }

    // --- 토큰 자동 첨부 테스트 ---

    @Test
    fun `토큰이 있으면 Authorization 헤더에 Bearer 토큰이 포함된다`() = runTest {
        val fakeDataSource = FakeLocalAuthDataSource(
            initialAccessToken = "valid-access-token",
            initialRefreshToken = "valid-refresh-token",
        )
        val authEventBus = AuthEventBus()
        val requestLog = mutableListOf<HttpRequestData>()

        val mainEngine = MockEngine { request ->
            requestLog.add(request)
            jsonResponse(successApiResponse())
        }
        val refreshEngine = MockEngine { jsonResponse(successApiResponse()) }

        val client = buildTestClient(fakeDataSource, authEventBus, mainEngine, refreshEngine)
        client.get("timetable")

        assertEquals(1, requestLog.size)
        assertEquals(
            "Bearer valid-access-token",
            requestLog[0].headers[HttpHeaders.Authorization],
        )
    }

    @Test
    fun `토큰이 없으면 Authorization 헤더가 포함되지 않는다`() = runTest {
        val fakeDataSource = FakeLocalAuthDataSource()
        val authEventBus = AuthEventBus()
        val requestLog = mutableListOf<HttpRequestData>()

        val mainEngine = MockEngine { request ->
            requestLog.add(request)
            jsonResponse(successApiResponse())
        }
        val refreshEngine = MockEngine { jsonResponse(successApiResponse()) }

        val client = buildTestClient(fakeDataSource, authEventBus, mainEngine, refreshEngine)
        client.get("timetable")

        assertEquals(1, requestLog.size)
        assertNull(requestLog[0].headers[HttpHeaders.Authorization])
    }

    @Test
    fun `인증 제외 경로에는 Authorization 헤더가 포함되지 않는다`() = runTest {
        val fakeDataSource = FakeLocalAuthDataSource(
            initialAccessToken = "valid-access-token",
            initialRefreshToken = "valid-refresh-token",
        )
        val authEventBus = AuthEventBus()
        val requestLog = mutableListOf<HttpRequestData>()

        val mainEngine = MockEngine { request ->
            requestLog.add(request)
            jsonResponse(successApiResponse())
        }
        val refreshEngine = MockEngine { jsonResponse(successApiResponse()) }

        val client = buildTestClient(fakeDataSource, authEventBus, mainEngine, refreshEngine)
        client.post("users/signin") {
            contentType(ContentType.Application.Json)
            setBody("""{"idToken":"test","nonce":"test"}""")
        }

        assertEquals(1, requestLog.size)
        assertNull(requestLog[0].headers[HttpHeaders.Authorization])
    }

    // --- 토큰 리프레시 + 재시도 테스트 ---

    @Test
    fun `401 응답 시 토큰 리프레시 후 새 토큰으로 재시도한다`() = runTest {
        val fakeDataSource = FakeLocalAuthDataSource(
            initialAccessToken = "expired-access-token",
            initialRefreshToken = "valid-refresh-token",
        )
        val authEventBus = AuthEventBus()
        val requestLog = mutableListOf<HttpRequestData>()

        val mainEngine = MockEngine { request ->
            requestLog.add(request)
            val authHeader = request.headers[HttpHeaders.Authorization]
            if (authHeader == "Bearer expired-access-token") {
                jsonResponse(errorApiResponse(), HttpStatusCode.Unauthorized)
            } else {
                jsonResponse(successApiResponse())
            }
        }

        val refreshLog = mutableListOf<HttpRequestData>()
        val refreshEngine = MockEngine { request ->
            refreshLog.add(request)
            jsonResponse(refreshSuccessResponse())
        }

        val client = buildTestClient(fakeDataSource, authEventBus, mainEngine, refreshEngine)
        val response = client.get("timetable")

        assertEquals(HttpStatusCode.OK, response.status)
        // 원래 요청(401) + 재시도(200) = 2번
        assertEquals(2, requestLog.size)
        assertEquals(
            "Bearer expired-access-token",
            requestLog[0].headers[HttpHeaders.Authorization],
        )
        assertEquals(
            "Bearer new-access-token",
            requestLog[1].headers[HttpHeaders.Authorization],
        )
        // 리프레시 1회 호출
        assertEquals(1, refreshLog.size)
        // 새 토큰이 로컬에 저장됨
        assertEquals("new-access-token", fakeDataSource.getAccessToken())
        assertEquals("new-refresh-token", fakeDataSource.getRefreshToken())
    }

    // --- 리프레시 실패 테스트 ---

    @Test
    fun `리프레시 실패 시 토큰 삭제 및 인증 만료 이벤트 발행`() = runTest {
        val fakeDataSource = FakeLocalAuthDataSource(
            initialAccessToken = "expired-access-token",
            initialRefreshToken = "expired-refresh-token",
        )
        val authEventBus = AuthEventBus()

        val events = mutableListOf<AuthEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            authEventBus.events.collect { events.add(it) }
        }

        val mainEngine = MockEngine {
            jsonResponse(errorApiResponse(), HttpStatusCode.Unauthorized)
        }
        val refreshEngine = MockEngine {
            jsonResponse(errorApiResponse(), HttpStatusCode.Unauthorized)
        }

        val client = buildTestClient(fakeDataSource, authEventBus, mainEngine, refreshEngine)
        val response = client.get("timetable")

        // 최종 응답은 401
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        // 토큰이 삭제됨
        assertNull(fakeDataSource.getAccessToken())
        assertNull(fakeDataSource.getRefreshToken())
        // 인증 만료 이벤트 발행됨
        assertTrue(events.any { it is AuthEvent.TokenExpired })
    }

    @Test
    fun `T04가 아닌 에러 응답 시 토큰 리프레시를 시도하지 않고 세션을 만료시킨다`() = runTest {
        val fakeDataSource = FakeLocalAuthDataSource(
            initialAccessToken = "expired-access-token",
            initialRefreshToken = "valid-refresh-token",
        )
        val authEventBus = AuthEventBus()

        val events = mutableListOf<AuthEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            authEventBus.events.collect { events.add(it) }
        }

        val mainEngine = MockEngine {
            jsonResponse(
                errorApiResponse(code = "T11", message = "RefreshToken이 일치하지 않습니다."),
                HttpStatusCode.Unauthorized,
            )
        }

        val refreshLog = mutableListOf<HttpRequestData>()
        val refreshEngine = MockEngine { request ->
            refreshLog.add(request)
            jsonResponse(refreshSuccessResponse())
        }

        val client = buildTestClient(fakeDataSource, authEventBus, mainEngine, refreshEngine)
        val response = client.get("timetable")

        // 최종 응답은 401
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        // 리프레시 API는 호출되지 않음
        assertEquals(0, refreshLog.size)
        // 토큰이 삭제됨
        assertNull(fakeDataSource.getAccessToken())
        assertNull(fakeDataSource.getRefreshToken())
        // 인증 만료 이벤트 발행됨
        assertTrue(events.any { it is AuthEvent.TokenExpired })
    }

    // --- 동시 요청 리프레시 테스트 ---

    @Test
    fun `동시 요청 시 리프레시는 1회만 수행된다`() = runTest {
        val fakeDataSource = FakeLocalAuthDataSource(
            initialAccessToken = "expired-access-token",
            initialRefreshToken = "valid-refresh-token",
        )
        val authEventBus = AuthEventBus()

        val mainEngine = MockEngine { request ->
            val authHeader = request.headers[HttpHeaders.Authorization]
            if (authHeader == "Bearer expired-access-token") {
                jsonResponse(errorApiResponse(), HttpStatusCode.Unauthorized)
            } else {
                jsonResponse(successApiResponse())
            }
        }

        val refreshLog = mutableListOf<HttpRequestData>()
        val refreshEngine = MockEngine { request ->
            refreshLog.add(request)
            jsonResponse(refreshSuccessResponse())
        }

        val client = buildTestClient(fakeDataSource, authEventBus, mainEngine, refreshEngine)

        val results = (1..3).map {
            async { client.get("timetable") }
        }.awaitAll()

        // 모든 요청이 성공
        results.forEach { response ->
            assertEquals(HttpStatusCode.OK, response.status)
        }
        // 리프레시는 1회만 수행
        assertEquals(1, refreshLog.size)
        // 새 토큰이 저장됨
        assertEquals("new-access-token", fakeDataSource.getAccessToken())
        assertEquals("new-refresh-token", fakeDataSource.getRefreshToken())
    }
}
