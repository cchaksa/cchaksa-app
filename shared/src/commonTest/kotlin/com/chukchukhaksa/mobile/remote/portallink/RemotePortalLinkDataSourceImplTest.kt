package com.chukchukhaksa.mobile.remote.portallink

import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJob
import com.chukchukhaksa.mobile.data.portallink.model.PortalLinkJobStatus
import com.chukchukhaksa.mobile.remote.common.ApiException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RemotePortalLinkDataSourceImplTest {

  private val requests = mutableListOf<HttpRequestData>()

  private fun dataSource(status: HttpStatusCode = HttpStatusCode.OK, body: String): RemotePortalLinkDataSourceImpl {
    val engine = MockEngine { request ->
      requests += request
      respond(
        content = ByteReadChannel(body),
        status = status,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
      )
    }
    // 앱 HttpClient와 같이 기본 URL이 /api/로 끝난다.
    val client = HttpClient(engine) {
      install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
      }
      defaultRequest {
        url("https://dev.api.cchaksa.com/api/")
        contentType(ContentType.Application.Json)
      }
    }
    return RemotePortalLinkDataSourceImpl(client)
  }

  @Test
  fun `create link job posts credentials to the host root with an idempotency key`() = runTest {
    val jobId = dataSource(
      status = HttpStatusCode.Accepted,
      body = """{"success":true,"data":{"job_id":"job-123","polling_endpoint":"/portal/link/jobs/job-123","status":"accepted"}}""",
    ).createLinkJob(studentId = "20231234", password = "pw", idempotencyKey = "key-1")

    assertEquals("job-123", jobId)
    val request = requests.single()
    assertEquals(HttpMethod.Post, request.method)
    assertEquals("https://dev.api.cchaksa.com/portal/link", request.url.toString())
    assertEquals("key-1", request.headers["Idempotency-Key"])
    val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
    assertEquals("suwon", body["portal_type"]?.jsonPrimitive?.content)
    assertEquals("20231234", body["username"]?.jsonPrimitive?.content)
    assertEquals("pw", body["password"]?.jsonPrimitive?.content)
  }

  @Test
  fun `job status maps server strings`() = runTest {
    val cases = mapOf(
      """{"status":"succeeded"}""" to PortalLinkJob(PortalLinkJobStatus.SUCCEEDED, null),
      """{"status":"failed","error_code":"PORTAL_AUTH_FAILED"}""" to PortalLinkJob(PortalLinkJobStatus.FAILED, "PORTAL_AUTH_FAILED"),
      """{"status":"running"}""" to PortalLinkJob(PortalLinkJobStatus.IN_PROGRESS, null),
      """{"status":"accepted"}""" to PortalLinkJob(PortalLinkJobStatus.IN_PROGRESS, null),
    )
    for ((data, expected) in cases) {
      val job = dataSource(body = """{"success":true,"data":$data}""").getLinkJob("job-123")
      assertEquals(expected, job)
    }
    assertEquals("https://dev.api.cchaksa.com/portal/link/jobs/job-123", requests.last().url.toString())
  }

  @Test
  fun `summary status is read from the summary endpoint`() = runTest {
    val status = dataSource(body = """{"success":true,"data":{"job_id":"job-123","status":"succeeded","studentInfo":{"name":"김척척"}}}""")
      .getLinkJobSummaryStatus("job-123")

    assertEquals(PortalLinkJobStatus.SUCCEEDED, status)
    assertEquals("https://dev.api.cchaksa.com/portal/link/jobs/job-123/summary", requests.single().url.toString())
  }

  @Test
  fun `upload wraps the raw scrape json as payload with an idempotency key`() = runTest {
    dataSource(body = UPLOAD_SUCCESS_BODY)
      .uploadScrapeResult("""{"student":{"sno":"20231234"},"semesters":[]}""", idempotencyKey = "key-1")

    val request = requests.single()
    assertEquals(HttpMethod.Post, request.method)
    assertEquals("https://dev.api.cchaksa.com/portal/link/scrape-results", request.url.toString())
    assertEquals("key-1", request.headers["Idempotency-Key"])
    val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
    assertEquals(setOf("portal_type", "payload"), body.keys)
    assertEquals("suwon", body["portal_type"]?.jsonPrimitive?.content)
    assertEquals(
      "20231234",
      body["payload"]?.jsonObject?.get("student")?.jsonObject?.get("sno")?.jsonPrimitive?.content,
    )
  }

  @Test
  fun `rejected upload becomes ApiException with its code`() = runTest {
    val error = assertFailsWith<ApiException> {
      dataSource(
        status = HttpStatusCode.Conflict,
        body = """{"success":false,"error":{"code":"C21","message":"현재 계정에 연결할 수 없는 학번입니다."}}""",
      ).uploadScrapeResult("""{"student":{}}""", idempotencyKey = "key-1")
    }

    assertEquals("C21", error.code)
  }

  @Test
  fun `server error with an error code is not treated as a rejection`() = runTest {
    assertFailsWith<ServerResponseException> {
      dataSource(
        status = HttpStatusCode.InternalServerError,
        body = """{"success":false,"error":{"code":"C18","message":"후처리 실패"}}""",
      ).uploadScrapeResult("""{"student":{}}""", idempotencyKey = "key-1")
    }
  }

  private companion object {
    // 백엔드 앱 연동 가이드의 성공 응답과 같은 모양이다.
    const val UPLOAD_SUCCESS_BODY = """{"success":true,"data":{"job_id":"job-123","status":"succeeded",""" +
      """"studentInfo":{"name":"김척척"},"finished_at":"2026-10-19T04:00:00Z"},"message":"요청 성공"}"""
  }
}
