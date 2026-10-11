package com.chukchukhaksa.mobile.remote.portalscrape

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * 실측한 수원대 포털·학사 시스템의 응답 흐름을 재현하는 가짜 서버.
 * - 로그인 성공·실패 모두 302이며, 실패하면 epTicket=LOG&epErrCode=... 로 돌아간다.
 * - 학사 SSO는 http://portal.suwon.ac.kr 로 리다이렉트한다.
 * - SSO 마지막 단계는 Path 속성이 없는 쿠키를 내려주고, 학사 API는 이 쿠키가 없으면 -998 오류를 준다.
 */
internal class FakeSuwonPortal {
  enum class LoginOutcome { SUCCESS, INVALID_USER, ACCOUNT_LOCKED }

  var loginOutcome = LoginOutcome.SUCCESS
  var ssoIssuesTicket = true
  var studentInfo: JsonObject = defaultStudentInfo()
  var courseSemesters: JsonElement = buildJsonArray {
    add(buildJsonObject { put("subjtEstbYear", "2025"); put("subjtEstbSmrCd", "10") })
  }
  var coursesBySemester: Map<String, JsonArray> = mapOf("2025-10" to defaultCourses())
  var gradeSummary: JsonObject = defaultGradeSummary()
  var creditsBySemester: Map<String, JsonArray> = mapOf("2025-10" to defaultCredits())
  var designatedResponse: JsonElement = buildJsonObject { put("precpSbjtList", JsonArray(emptyList())) }

  val requests = mutableListOf<HttpRequestData>()

  val engine = MockEngine { request ->
    requests += request
    route(request)
  }

  fun client(): HttpClient = HttpClient(engine) { configurePortalClient() }

  fun requestsTo(path: String) = requests.filter { it.url.encodedPath == path }

  private fun MockRequestHandleScope.route(request: HttpRequestData): HttpResponseData {
    val url = request.url
    val cookies = request.headers[HttpHeaders.Cookie].orEmpty()
    return when ("${url.host}${url.encodedPath}") {
      "portal.suwon.ac.kr/enview/index.jsp" ->
        redirect("https://portal.suwon.ac.kr/enview/portal/;jsessionid=PORTAL1", "JSESSIONID=PORTAL1; Path=/enview")

      "portal.suwon.ac.kr/enview/portal/;jsessionid=PORTAL1" -> html("<html>portal</html>")

      "portal.suwon.ac.kr/enpass/login" -> when {
        request.method == HttpMethod.Post -> when (loginOutcome) {
          LoginOutcome.SUCCESS -> redirect(
            "https://portal.suwon.ac.kr/enview/user/enpassLoginProcess.face?epTicket=LOGIN_TICKET",
            "ENPASSTGC=TGC1; Path=/enpass",
          )

          LoginOutcome.INVALID_USER ->
            redirect("https://portal.suwon.ac.kr/enview/user/enpassLoginProcess.face?epTicket=LOG&epErrCode=A02")

          LoginOutcome.ACCOUNT_LOCKED ->
            redirect("https://portal.suwon.ac.kr/enview/user/enpassLoginProcess.face?epTicket=LOG&epErrCode=A05")
        }

        ssoIssuesTicket && "ENPASSTGC=TGC1" in cookies ->
          redirect("https://info.suwon.ac.kr/sso_security_check?epTicket=SSO_TICKET")

        else -> redirect("https://info.suwon.ac.kr/sso_security_check?epTicket=LOG")
      }

      "portal.suwon.ac.kr/enview/user/enpassLoginProcess.face" -> when (url.parameters["epErrCode"]) {
        null -> redirect("https://portal.suwon.ac.kr/enview/portal/USW_STU/USW_STU_DEFAULT.page")
        "A02" -> html(portalPageWithError("유효하지 않은 사용자입니다."))
        else -> html(portalPageWithError("비밀번호를 연속 5회 잘못 입력하셨습니다. 비밀번호를 재발급 받으세요."))
      }

      "portal.suwon.ac.kr/enview/portal/USW_STU/USW_STU_DEFAULT.page" -> html("<html>main</html>")

      "info.suwon.ac.kr/sso_security_check" -> when (url.parameters["epTicket"]) {
        null -> redirect(
          "http://portal.suwon.ac.kr/enpass/login?gateway=client&service=https://info.suwon.ac.kr/sso_security_check",
          "JSESSIONID=INFO1; Path=/; Secure",
        )

        "SSO_TICKET" -> redirect("https://info.suwon.ac.kr/websquare/websquare.jsp?w2xPath=/views/main.xml", "userNm=TESTER")
        else -> html("<title>로그인 실패!!</title>")
      }

      "info.suwon.ac.kr/websquare/websquare.jsp" -> html("<html>websquare</html>")

      else -> if (url.host == "info.suwon.ac.kr" && request.method == HttpMethod.Post) {
        if ("JSESSIONID=INFO1" in cookies && "userNm=TESTER" in cookies) {
          academicApi(url.encodedPath, request.jsonBody())
        } else {
          json("""{"errorMessage":"로그인 하지 않았거나, 세션이 종료되었습니다.","errorCode":"-998"}""")
        }
      } else {
        respond("", HttpStatusCode.NotFound)
      }
    }
  }

  private fun MockRequestHandleScope.academicApi(path: String, body: JsonObject): HttpResponseData = when (path) {
    "/scrgBas/selectScrgBas.do" -> json(buildJsonObject { put("studentInfo", studentInfo) }.toString())
    "/atlecApplDtai/listAtlecApplDtaiTabYearSmr.do" ->
      json(buildJsonObject { put("listAtlecApplDtaiTabYearSmr", courseSemesters) }.toString())

    "/atlecApplDtai/listAtlecApplDtaiTabSubjt.do" -> {
      val key = "${body.text("subjtEstbYear")}-${body.text("subjtEstbSmrCd")}"
      json(buildJsonObject { put("listAtlecApplDtaiTabSubjt", coursesBySemester[key] ?: JsonArray(emptyList())) }.toString())
    }

    "/smrCretSum/listSmrCretSumTabYearSmrStud.do" -> json(gradeSummary.toString())
    "/cretBas/listSmrCretSumTabSubjt.do" -> {
      val key = "${body.text("cretGainYear")}-${body.text("cretSmrCd")}"
      creditsBySemester[key]
        ?.let { json(buildJsonObject { put("listSmrCretSumTabSubjt", it) }.toString()) }
        ?: respond("", HttpStatusCode.InternalServerError)
    }

    "/precpSbjt/listPrecpSbjt.do" -> json(designatedResponse.toString())
    else -> respond("", HttpStatusCode.NotFound)
  }

  private fun MockRequestHandleScope.redirect(location: String, setCookie: String? = null) = respond(
    content = "",
    status = HttpStatusCode.Found,
    headers = headersOf(
      *listOfNotNull(
        HttpHeaders.Location to listOf(location),
        setCookie?.let { HttpHeaders.SetCookie to listOf(it) },
      ).toTypedArray(),
    ),
  )

  private fun MockRequestHandleScope.html(body: String) = respond(
    content = body,
    status = HttpStatusCode.OK,
    headers = headersOf(HttpHeaders.ContentType, "text/html;charset=utf-8"),
  )

  private fun MockRequestHandleScope.json(body: String) = respond(
    content = body,
    status = HttpStatusCode.OK,
    headers = headersOf(HttpHeaders.ContentType, "application/json;charset=UTF-8"),
  )

  companion object {
    fun portalPageWithError(message: String) = """
      <script>
        function initEnview() {
          var errorMessage = "$message";
          if( errorMessage != "null" && errorMessage.length > 0 ) {
            alert( errorMessage );
          }
        }
      </script>
    """.trimIndent()

    fun defaultStudentInfo() = buildJsonObject {
      put("sno", "24020044")
      put("studNm", "테스트 학생")
      put("univCd", "2000510")
      put("univNm", "ICT융합대학")
      put("dpmjCd", "2000513")
      put("dpmjNm", "정보통신학부")
      put("mjorCd", "2000516")
      put("mjorNm", "정보통신")
      put("studIidno", "포함되면 안 되는 민감 정보")
      put("enscYear", "2024")
      put("enscSmrCd", "10")
      put("scrgStatNm", "재학")
      put("studGrde", 2)
      put("enscDvcd", "1")
      put("facSmrCnt", "3")
      put("flangPassGb", "미통과")
    }

    fun defaultCourses() = buildJsonArray {
      add(
        buildJsonObject {
          put("subjtEstbYear", "2025")
          put("subjtCd", "04048")
          put("subjtNm", "자료구조")
          put("point", 3)
          put("subjtEstbSmrCd", "10")
        },
      )
      add(
        buildJsonObject {
          put("subjtEstbYear", "2025")
          put("subjtCd", "05001")
          put("subjtNm", "채플")
          put("point", null as Int?)
          put("subjtEstbSmrCd", "10")
        },
      )
    }

    fun defaultGradeSummary() = buildJsonObject {
      put("listSmrCretSumTabFac", JsonArray(emptyList()))
      put(
        "listSmrCretSumTabYearSmr",
        buildJsonArray { add(buildJsonObject { put("cretGainYear", "2025"); put("cretSmrCd", "10"); put("gainPoint", 4) }) },
      )
      put(
        "selectSmrCretSumTabSjTotal",
        buildJsonObject { put("gainPoint", "4"); put("applPoint", "4"); put("gainAvmk", "4.25"); put("gainTavgPont", "93.2") },
      )
    }

    fun defaultCredits() = buildJsonArray {
      add(buildJsonObject { put("subjtCd", "04048"); put("cretGrdCd", "A+"); put("gainPoint", 3) })
      add(buildJsonObject { put("subjtCd", "05001"); put("cretGrdCd", "P"); put("gainPoint", 1) })
    }
  }
}

internal fun HttpRequestData.jsonBody(): JsonObject =
  Json.parseToJsonElement((body as TextContent).text).jsonObject

private fun JsonObject.text(key: String) = this[key]?.jsonPrimitive?.content
