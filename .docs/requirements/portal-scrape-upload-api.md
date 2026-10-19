# 앱 스크래핑 결과 업로드 API 계약

- 작성일: 2026-10-19
- 상태: 백엔드가 앱 초안을 받아 구현했다. 백엔드 [PR #369](https://github.com/cchaksa/cchaksa-backend/pull/369)는 2026-10-19 기준 Draft이며 아직 배포되지 않았다.
- 원본 계약: 백엔드 `feat/368` 브랜치의 [`docs/tasks/368/app-integration-guide.md`](https://github.com/cchaksa/cchaksa-backend/blob/feat/368/docs/tasks/368/app-integration-guide.md)(구현 기준 `a616579c`).
  이 문서는 그 가이드에서 앱이 따르는 부분과 앱의 처리를 정리한 것이다. 둘이 다르면 가이드가 맞다.
- 앱 구현: `RemotePortalLinkDataSourceImpl.uploadScrapeResult`(`shared/src/commonMain/.../remote/portallink/`),
  `PortalLinkRepositoryImpl.link`·`upload`(`.../data/portallink/repository/`)

## 배경

앱은 포털 스크래핑을 기기에서 직접 한다(`remote/portalscrape`, suwon-scraper-v1을 Ktor로 옮긴 코드).
서버에서 스크래핑 결과를 받는 경로는 내부 콜백(`POST /internal/scrape-results`)뿐이었다. 이 콜백은 HMAC 비밀 키로 서명해야 하고
결과 본문 대신 S3 키를 받으므로 앱이 쓸 수 없다. 그래서 앱이 결과 본문을 직접 올리는 엔드포인트를 백엔드에 요청했다.

졸업 요건 계산(`GET /api/graduation/progress`)과 학업 정보 조회는 지금처럼 서버가 한다.

## 요청

```
POST /portal/link/scrape-results
Authorization: Bearer <서비스 access token>
Content-Type: application/json
Idempotency-Key: <스크래핑 결과 한 건에 만든 UUID>
```

기존 포털 연동 API와 같이 `/api` 아래가 아니라 호스트 바로 아래에 있다(`https://api.cchaksa.com/portal/link/scrape-results`).

```json
{
  "portal_type": "suwon",
  "payload": {
    "student": { "sno": "20231234", "studNm": "...", "...": "..." },
    "semesters": [ { "semester": "2024-10", "courses": [ ... ] } ],
    "academicRecords": { "listSmrCretSumTabYearSmr": [ ... ], "selectSmrCretSumTabSjTotal": { ... } },
    "designatedCourses": [ ... ]
  }
}
```

- `payload`는 `PortalScrapeResult.rawJson`을 JSON 객체로 파싱한 값이다. 문자열로 넣지 않는다.
- 서버는 `payload`를 전체 기록으로 보고 저장한다. 올린 수강 목록에서 빠진 기존 과목은 지워질 수 있고,
  `designatedCourses: []`는 기존 지정과목을 지운다. 편입생이 아니면 스크래퍼가 `[]`를 넣는데, 이 학생들은 지정과목이 없으므로 문제가 없다.
- 선택 필드 `warnings`는 보내지 않는다. 경고가 하나라도 있는 결과는 아예 올리지 않기 때문이다(아래 「앱 처리」).
- 포털 비밀번호·쿠키는 보내지 않는다.

## 응답

성공하면 학사 정보와 작업 이력을 모두 저장한 뒤 `200`을 돌려준다. 서버 job과 달리 폴링하지 않는다.

```json
{ "success": true, "data": { "job_id": "...", "status": "succeeded", "studentInfo": { ... }, "finished_at": "..." }, "message": "요청 성공" }
```

앱은 `success`만 확인하고, 성공하면 홈 웹뷰를 다시 불러와 학사·졸업 정보를 새로 조회한다.

실패하면 다른 API와 같은 형식으로 `error.code`를 담아 준다.

| HTTP | 코드 | 의미 |
|---|---|---|
| 400 | `C01`·`C09` | 요청 형식 오류, 지원하지 않는 포털 |
| 401 | 인증 오류 코드 | 서비스 토큰 만료 등 |
| 409 | `C07` | 같은 키로 다른 본문을 보냈다 |
| 409 | `C21` | 학번이 다른 계정에 연결됐거나, 이미 연결된 학번과 다르다 |
| 409 | `C22` | 이 결과보다 최신인 포털 데이터가 이미 반영됐다 |
| 422 | `C17` | `payload` 검증 실패(구조, 필수값, 수치, 중복, 목록 크기) |
| 422 | `C20` | `warnings`에 부분 수집 실패가 있다 |
| 5xx | 없을 수도 있음 | 일시 실패. 저장 도중 실패하면 서버가 전부 롤백한다 |

## 앱 처리

키는 스크래핑 결과가 확정될 때 한 번 만든다(`PortalScrapeUpload`). 그 결과를 다시 올리는 동안에는 같은 키와 같은 본문을 쓰고,
새로 스크래핑하면 새 키를 만든다. 서버 fallback job의 키(`POST /portal/link`)와는 따로 만든다.

| 상황 | 앱 동작 |
|---|---|
| 스크래핑 결과에 경고가 있다(일부 학기 성적 상세 조회 실패) | 올리지 않는다. 다른 기기 쪽 실패와 같이 서버 job으로 넘어간다 |
| `200` | 연동 완료. 홈 웹뷰를 다시 불러온다 |
| `C22` | 더 최신 데이터가 이미 있으므로 이 결과는 버리고 연동 완료로 처리한다 |
| `C21` | 안내 문구를 보여 준다. 다시 시도하면 입력 폼으로 돌아간다 |
| 그 밖의 4xx(`C01`·`C07`·`C09`·`C17`·`C20`, 토큰 갱신에 실패한 401 등) | 같은 결과를 다시 올리지 않는다. 실패 화면을 보여 주고, 다시 시도하면 입력 폼으로 돌아가 새로 스크래핑한다 |
| 5xx, 네트워크 오류, 제한 시간(60초) 초과, 해석할 수 없는 응답 | 저장됐는지 알 수 없으므로 같은 키·같은 본문으로 1초, 2초 간격으로 최대 3번까지 보낸다. 그래도 실패하면 실패 화면의 「다시 시도하기」가 같은 결과를 같은 키로 다시 올린다 |

- 업로드가 실패해도 서버 job으로 넘기지 않는다. 백엔드 가이드도 `C17`·`C20`·`C21`·`C22`를 fallback의 근거로 쓰지 말라고 한다.
- 만료된 토큰(`T04`)은 HTTP 클라이언트의 Auth 플러그인이 갱신한 뒤 같은 요청을 다시 보낸다.
- 다시 올릴 결과와 키는 화면의 ViewModel에만 있다. 화면을 벗어나거나 4xx로 거부되면 버리므로, 다른 계정에서 이전 결과를 다시 올리지 않는다.
- 같은 화면에서는 업로드를 한 번에 하나만 진행한다.

## 서버 job 경로(fallback)

기기 스크래핑이 네트워크 오류, 포털 구조 변경, SSO 실패처럼 기기 쪽 문제로 실패하거나 결과에 경고가 있으면 앱은 기존 서버 job으로 넘어간다.
이 경로는 웹과 같은 API를 그대로 쓴다.

- `POST /portal/link`(`Idempotency-Key` 헤더 포함) → `GET /portal/link/jobs/{jobId}`를 2초 간격으로 최대 3분 동안 폴링
- 3분이 지나면 `GET /portal/link/jobs/{jobId}/summary`로 성공 여부를 한 번 더 확인
- 비밀번호 오류(`PORTAL_AUTH_FAILED`)와 계정 잠김(`PORTAL_ACCOUNT_LOCKED`)은 서버 job으로 넘기지 않는다

백엔드 가이드에 따르면 학교의 클라우드 접근 제한을 피하는 통신사 IP 구성은 이번 백엔드 작업에 들어 있지 않다.
그래서 fallback이 실제로 동작하는지는 따로 확인해야 한다.

## 백엔드가 정한 위험 처리

- **데이터 위변조**: 앱이 보낸 `payload`는 서버가 진위를 확인할 수 없다. 백엔드는 JWT의 본인 계정에만 반영하고,
  다른 계정의 학번 선점이나 연결된 학번 변경은 `C21`로 거절한다. 앱 결과로는 계정을 자동 병합하지 않는다.
- **입력 제한**: 학기 100개, 학기당 과목 100개, 전체 과목 3,000개, 지정과목 500개.

## 배포 후 확인할 연동 시나리오

백엔드 가이드의 「함께 확인할 연동 시나리오」를 실제 앱에서 확인한다. 실동작 QA는 사람이 한다.

- 최초 연동과 재연동 갱신이 모두 성공한다.
- 같은 키·본문을 다시 보내면 같은 `job_id`를 받고, 새로 스크래핑한 결과는 새 키로 반영된다.
- 업로드 제한 시간 초과 뒤 다시 시도해도 두 번 반영되지 않는다.
- 일부 학기 조회가 실패하면 업로드하지 않고, 정상적인 빈 목록(신입생, 지정과목 없는 편입생)은 올바르게 반영된다.
- `C21`·`C22`에서 반복 업로드하지 않는다.
