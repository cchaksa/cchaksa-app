# 참고 석차 원본 수집·전송

- 작성일: 2026-11-05
- 원본 계약: 백엔드 [PR #378](https://github.com/cchaksa/cchaksa-backend/pull/378)(2026-11-05 KST 병합, 병합 커밋 `98fb52f2`, PR 최종 커밋 `4e7e1ce5`)의
  `docs/tasks/376/mobile-integration-guide.md`. 이 문서는 앱이 그 계약을 어떻게 따르는지 정리한 것이다. 둘이 다르면 가이드가 맞다.
- 앱 구현: `remote/portalscrape/ReferenceRankingRawCollector.kt`, `PortalAcademicApi.kt`(학사 API 원문 기록),
  `SuwonPortalScraper.scrapeAuthenticatedData`(수집 단계 `REFERENCE_RANKING`)

## 역할 분담

앱은 포털 응답을 모아 기존 업로드(`POST /portal/link/scrape-results`)의 `payload.referenceRankingRaw`에 넣는다.
신청평점·동점 기준·순위 계산은 모두 서버가 한다. 앱은 평점이나 `referenceRanking`을 만들지 않는다.

## 수집 순서

학사 자료 조회(학생·수강·성적)가 끝난 뒤에 모은다.

1. 등록(`REGISTRATION`)과 전공 이력(`MAJOR_HISTORY`)을 동시에 조회한다.
2. 성적 요약(`GRADE_SUMMARY`)과 수강 학기 목록(`COURSE_TERMS`)은 학사 자료 조회에서 받은 응답을 그대로 쓴다.
3. 두 목록에 있는 정규학기와 그 바로 이전 정규학기마다 성적 상세(`GRADE_DETAIL`)와 수강 상세(`COURSE_DETAIL`)를 넣는다.
   학사 자료 조회에서 이미 받은 학기는 다시 조회하지 않고, 목록에 없는 이전 학기만 새로 조회한다. 계절학기(15·25)는 넣지 않는다.
4. 전공 이력에서 취소되지 않은 `주전공배정` 행의 학기마다 전공배정(`MAJOR_ASSIGNMENT`, `mjorChngCd=11`)을 조회한다.

같은 스크래핑에서 받은 응답은 `PortalAcademicExchangeLog`에 남는다. 그래서 응답 원문을 다시 직렬화하지 않고 받은 텍스트 그대로 보낼 수 있다.

## 응답 처리

| 응답 | 보내는 값 |
|---|---|
| 성적·수강 4종 | 받은 텍스트 그대로. 숫자 정밀도, null, F·R·P, 삭제·재수강 표시를 바꾸지 않는다 |
| 학적 3종 | 대상 필드(`listTutnTab`, `listMjorChngInfoTab`, `mjorChngAppl`)만 남기고, 그 안에서도 가이드의 허용 필드만 남긴다. `listSchoPaytTab`은 보내지 않는다 |
| 정상 빈 목록, 전공배정 `null`·`{}` | `EMPTY` |
| 응답을 받지 못함 | `FAILED`, `NETWORK_ERROR`, `httpStatus=null` |
| HTTP 2xx가 아님 | `FAILED`, `HTTP_ERROR` |
| 세션 만료(`errorCode=-998`) | `FAILED`, `SESSION_EXPIRED` |
| 로그인 HTML, 대상 필드 누락·형식 오류, 다른 학번(`sno`), 다른 조회 학기, 100행·200,000자 초과 | `FAILED`, `INVALID_RESPONSE` |

서버 검증(`AppReferenceRankingRawValidator`)에 걸릴 응답은 앱이 먼저 `FAILED`로 바꾼다. 서버는 원본 하나라도 계약에 맞지 않으면
업로드 전체를 `422 C17`로 거절하기 때문이다.

## 기본 학사 업로드를 지키는 장치

참고 석차 원본은 부가 자료다. 원본 때문에 연동이 실패하지 않게 한다.

- 일부 학기 성적 조회가 실패한 결과(경고 있음)는 업로드하지 않으므로 원본도 모으지 않는다.
- 수집은 최대 30초, 그리고 스크래핑 전체 제한 시간(120초)에서 5초를 남긴 시간 안에서만 한다. 시간을 넘기거나 예상하지 못한 오류가 나면 원본 없이 올린다.
- 응답 245개 또는 원문 합계 2,000,000바이트를 넘으면 원본을 보내지 않는다. 생략하면 서버는 기존 석차 자료를 유지한다.
- 원본을 넣은 업로드가 `C17`로 거절되면, 원본을 빼고 새 `Idempotency-Key`로 기본 학사 자료만 다시 올린다.

## 앱이 하지 않는 일

- 참고 석차 조회(`GET /api/academic-records/reference-rank?year=&semester=`)와 결과 표시는 웹이 맡는다(2026-11-05 결정).
  성적 화면이 웹뷰에 있기 때문이다. 앱은 원본 수집·전송까지 한다.
- 서버 JSON 파서는 중복 키를 거부한다(`STRICT_DUPLICATE_DETECTION`). 앱은 중복 키를 따로 검사하지 않고, 그런 응답이 오면 위의 `C17` 재업로드로 처리된다.

## 배포 후 확인할 시나리오

실동작 QA는 사람이 한다. 백엔드 변경이 연동 대상 서버에 배포됐는지 먼저 확인한다.

- 최초 연동과 재연동에서 업로드가 `200`, `data.status=succeeded`로 끝난다.
- 과거 학기와 이전 정규학기 원본이 함께 올라가서, 과거 학기의 참고 석차도 조회된다.
- 학적 조회가 실패해도 기본 학사 자료는 반영된다.
- 같은 결과를 다시 올릴 때 같은 키·같은 본문을 쓴다.
