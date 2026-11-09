---
name: release
description: 척척학사 앱을 배포한다. 정식 모드는 스토어 테스트 트랙(Play 내부 테스트, TestFlight)에 올리고 버전 파일 커밋·태그·main push까지 한다. QA 모드는 Android를 Firebase App Distribution에, iOS를 TestFlight 내부 테스터에게 debug 또는 release 빌드로 올리고 빌드 번호 커밋을 현재 브랜치에 push한다. 시작하기 전에 모드, 플랫폼, 버전·빌드 타입, 노트를 묻는다. 트리거 — "/release", "배포해줘", "릴리스하자", "스토어에 올려줘", "TestFlight에 올려줘", "내부 테스트 트랙에 올려줘", "3.2.5 배포", "QA 배포", "QA 빌드 올려줘", "파이어베이스로 배포", "테스터한테 빌드 보내줘". 프로덕션 출시와 App Store 심사 제출은 하지 않는다(콘솔에서 사람이 한다). fastlane 설정 자체를 고치는 작업에는 쓰지 않는다.
---

# 척척학사 배포

fastlane lane(`fastlane/Fastfile`)을 감싸는 절차다. 모드가 두 개 있다.

| | 정식 모드 | QA 모드 |
|---|---|---|
| Android | Play 내부 테스트 트랙(AAB, release) | Firebase App Distribution '척척학사' 그룹(APK, debug 또는 release) |
| iOS | TestFlight(Release) | TestFlight 내부 테스터(Debug 또는 Release) |
| 브랜치 | main만 | 현재 브랜치 |
| 버전 | 다음 버전을 묻는다 | 묻지 않는다(iOS 버전이 닫혀 있을 때만 묻는다) |
| 커밋·태그·push | 버전 커밋 + 태그 + main push | 빌드 번호 커밋 + 현재 브랜치 push, 태그 없음 |
| 노트 | 사용자용 초안을 보여 주고 확인받는다 | 자동으로 만들어 최종 확인에서 보여 준다 |

**모드 선택**: 사용자의 말에 "QA"나 "파이어베이스", "테스터"가 있으면 QA 모드, "출시", "릴리스", "스토어", 버전 번호가 있으면 정식 모드다. 판단이 서지 않으면 AskUserQuestion으로 `정식 배포` / `QA 배포`를 묻는다. 정식 모드는 0~3절, QA 모드는 4절을 따른다.

그 뒤의 프로덕션 출시와 심사 제출은 사람이 콘솔에서 한다. lane과 키 위치는 `CLAUDE.md`의 "Release Automation (fastlane)" 절에 있다. 모든 명령은 리포 루트에서 실행한다.

## 0. 사전 점검 (질문하기 전에)

하나라도 어긋나면 질문을 시작하지 말고, 무엇이 어긋났는지 보고하고 멈춘다. 고치는 일(pull, rebase, stash, 키 발급)은 사용자에게 묻고 한다.

1. 현재 브랜치가 `main`이다.
2. `git status --porcelain --untracked-files=no`가 비어 있다. 추적 파일에 커밋하지 않은 변경이 있으면 그 변경이 버전 커밋에 섞인다.
3. `git fetch origin` 후 `HEAD`가 `origin/main`과 같다.
4. `~/.config/chukchukhaksa/`에 `play-service-account.json`과 `asc-api-key.env`가 있다. 파일 내용은 읽지 않는다. `local.properties`도 읽지 않는다. Android 서명 값이 없으면 lane이 빌드 전에 알려 준다.
5. `bundle check`가 실패하면 `bundle install`을 실행한다.

그다음 스토어 상태를 읽는다. 두 lane은 **차례로** 실행한다.

```bash
bundle exec fastlane android status   # → fastlane/output/status-android.json
bundle exec fastlane ios status       # → fastlane/output/status-ios.json
```

- Android: `repo`(versionName, versionCode), `play`(트랙별 versionCode와 release name. release name은 보통 `59 (3.2.4)` 형태라 괄호 안이 versionName이다), `last_tag`.
- iOS: `repo`(MARKETING_VERSION, 빌드), `asc_latest_upload`(TestFlight에 마지막으로 올라간 버전과 빌드), `app_store_live`(출시된 버전), `last_tag`.

## 1. 질문 (AskUserQuestion)

질문 문구 안에 판단 근거(현재 버전 상태)를 함께 적는다. 질문 창만 보고도 고를 수 있어야 한다.

### 1-1. 대상 플랫폼

옵션: `Android와 iOS 둘 다` / `Android만` / `iOS만`. 문구에 두 플랫폼의 리포 버전과 스토어 버전을 한 줄씩 적는다.

### 1-2. 다음 버전

고른 플랫폼에 대해 후보를 계산한다.

- **기준 버전**: 고른 플랫폼의 리포 버전, Play의 모든 트랙 release name에 있는 versionName, ASC 최신 업로드 버전, App Store 출시 버전 중 가장 높은 것.
- **"같은 버전에 빌드만 추가 (X)"**: 아직 출시되지 않은 버전 X가 있을 때만 넣는다. iOS는 `asc_latest_upload.version`이 `app_store_live.version`보다 높으면 그 버전이다. 출시된 버전의 train은 닫혀 있어서 ASC가 새 빌드를 거절한다. 심사를 통과해 출시 대기(Pending Developer Release) 중인 버전도 train이 닫혀 있을 수 있는데 status에는 나오지 않으므로, 그런 버전이 있는지 모르면 문구에 "심사를 통과한 버전이면 업로드가 거절된다"고 적는다. Android는 리포 versionName이 production 트랙의 versionName보다 높으면 그 버전이다.
- **patch / minor / major**: 기준 버전에서 계산한다(예: 3.2.5 → 3.2.6 / 3.3.0 / 4.0.0).
- 권장(첫 옵션, "(권장)" 표시): "빌드만 추가"가 있으면 그것, 없으면 patch.
- 두 플랫폼을 함께 배포하면 고른 버전 하나를 두 플랫폼에 같이 쓴다. 두 플랫폼의 현재 버전이 다르면 문구에 그 사실을 적는다.

### 1-3. 릴리스 노트

1. 범위: 고른 플랫폼의 `last_tag`부터 `HEAD`까지. 두 플랫폼이면 더 오래된 태그부터 시작한다. 태그가 없으면 최근 30개 커밋을 쓴다.
2. `git log --no-merges --format='%h %s%n%b' <tag>..HEAD`를 읽고 **사용자용 한국어 초안**을 쓴다.
   - 사용자가 체감하는 변경(기능, 화면, 버그 수정)만 남긴다. `chore`, `refactor`, `test`, `ci`, `docs`, 버전 커밋, 빌드·배포 도구 변경은 뺀다.
   - "- "로 시작하는 짧은 항목 2~5개. 커밋 해시나 내부 용어(클래스명, API 경로)는 쓰지 않는다.
   - **500자 이하**로 쓴다(Play 한도. TestFlight에도 같은 문구를 쓴다).
   - 이모지와 `<` 문자는 쓰지 않는다. TestFlight(pilot)는 이 문자들을 지우고 Play는 그대로 두어서, 두 스토어의 문구가 달라진다.
   - 사용자에게 보이는 변경이 없으면 "버그 수정 및 안정성 개선"처럼 한 줄로 쓴다.
3. 질문 문구에 초안 전체를 넣고 옵션을 준다: `이대로 사용` / `노트 없이 진행`. 고치고 싶으면 사용자가 Other에 고친 문구를 적는다. Other로 받은 문구도 500자를 넘으면 줄여 달라고 다시 묻는다.
4. 확정된 문구를 `fastlane/output/release-notes.txt`에 UTF-8로 저장한다(`fastlane/output/`은 gitignore 대상이고, status lane이 만들어 둔다).

### 1-4. 최종 확인

업로드는 되돌릴 수 없으므로, 실행 전에 한 번 확인한다. 문구에 플랫폼, 버전, 예상 versionCode·빌드 번호(Android는 Play 최대값+1과 리포 versionCode+1 중 큰 값, iOS는 ASC 최신 빌드+1), 노트 유무, 업로드 뒤 main push 예정을 적는다. 옵션: `진행` / `취소`.

## 2. 실행

플랫폼마다 아래 순서를 밟는다. **Android를 먼저, iOS를 나중에, 하나씩** 실행한다. 두 lane이 같은 Gradle 프로젝트를 쓰므로 동시에 돌리면 잠금이 충돌한다. lane은 오래 걸리므로(Android 2~5분, iOS 6~15분) 백그라운드로 실행하고 완료 알림을 기다린다. 기다리는 동안 사용자에게 어느 단계인지 짧게 알린다.

1. **버전 반영**: 고른 버전이 그 플랫폼의 리포 버전과 다를 때만 실행한다.
   ```bash
   bundle exec fastlane bump version:<V> platform:android   # 또는 platform:ios
   ```
2. **업로드**: 노트가 있으면 `notes_file`을 붙인다. 출력은 `fastlane/output/<platform>-upload.log`로 남긴다.
   ```bash
   bundle exec fastlane android internal notes_file:fastlane/output/release-notes.txt > fastlane/output/android-upload.log 2>&1
   bundle exec fastlane ios beta notes_file:fastlane/output/release-notes.txt > fastlane/output/ios-upload.log 2>&1
   ```
3. **성공하면** 그 플랫폼의 버전 파일만 커밋하고 태그를 붙인다. iOS 로그에 `was uploaded, but setting "What to Test" failed`가 있으면 업로드는 성공한 것이다. 성공으로 처리하고, 보고에 TestFlight 노트를 App Store Connect에서 직접 넣어야 한다고 적는다. lane이 올린 versionCode·빌드 번호는 파일에서 다시 읽는다.
   - Android: `git add androidApp/build.gradle.kts`, 메시지 `chore(android): 버전 <V> (versionCode <N>)`, 태그 `v<V>-android`
   - iOS: `git add iosApp/iosApp.xcodeproj/project.pbxproj`, 메시지 `chore(ios): 버전 <V> (빌드 <N>)`, 태그 `v<V>-ios`
   - 태그는 기존 관례대로 lightweight 태그다(`git tag v<V>-android`). 같은 이름의 태그가 이미 있으면(같은 버전에 빌드만 추가한 경우) 옮기지 않고, 새 태그를 만들지 않았다고 보고에 적는다.
   - 커밋 메시지 끝에는 세션의 attribution 줄을 붙인다.
4. **실패하면** 그 플랫폼의 버전 파일 변경을 되돌리고(`git checkout -- <파일>`), 로그 끝 30줄에서 원인을 찾아 기록한다. 다른 플랫폼은 독립적이므로 계속 진행한다. 아래 실패는 원인이 분명하니 다시 시도하지 말고 보고한다.

   | 메시지 | 원인과 조치 |
   |---|---|
   | `missing upload-key signing values` | `local.properties`에 `RELEASE_*` 값이 없다. 사용자가 직접 넣어야 한다 |
   | `older than ... on App Store Connect` / `already live` / `closed for new build submissions` | 버전 선택이 잘못됐다. 1-2로 돌아가 다시 묻는다 |
   | `PLA Update available`, `No signing certificate "iOS Distribution"` | Apple 개발자 프로그램 약관이 갱신됐다. Account Holder가 developer.apple.com에서 동의해야 한다 |
   | `Could not read any Play track` | Play 서비스 계정 키나 권한 문제다 |

5. **push**: 커밋이 하나라도 생겼으면 `git fetch origin` 후 `origin/main`이 그대로인지 확인하고 `git push origin main`, 이어서 새 태그만 `git push origin <tag> ...`로 올린다. 그사이 `origin/main`이 움직였으면 push하지 말고 사용자에게 묻는다. `--force`는 쓰지 않는다.

## 3. 보고

다음을 짧게 보고한다.

- 플랫폼별 결과: 버전, versionCode·빌드 번호, 업로드 성공 여부(실패면 원인)
- 사용한 릴리스 노트
- 만든 커밋과 태그, push 여부
- 사람이 할 남은 일:
  - Android: Play Console에서 내부 테스트 출시를 확인하고, 필요하면 프로덕션으로 승격한다.
  - iOS: TestFlight 처리(보통 10~30분)가 끝나면 App Store Connect에서 심사에 제출한다. **심사에 제출할 빌드는 `<V> (<N>)`이다**라고 번호를 적는다. 같은 버전에 QA 빌드(테스트할 내용 첫 줄이 `[QA ... - 심사 제출 금지]`)가 올라가 있을 수 있어서, 가장 최근 빌드를 고르면 QA 빌드가 제출될 수 있다. 제출 화면에서 연령 등급 설문이나 수출 규정 문항을 요구할 수 있다.

## 4. QA 모드

정식 모드와 같은 lane 구조를 쓰되, 질문이 적고 태그가 없다. 2절의 실행 원칙(Android 먼저, 하나씩, 백그라운드 실행)과 실패 표는 QA 모드에도 그대로 적용한다.

### 4-1. 사전 점검

0절과 같되 다음이 다르다.

1. 브랜치는 `main`이 아니어도 된다. detached HEAD면 멈춘다.
2. `git fetch origin` 후 현재 브랜치에 upstream이 있으면 `HEAD`가 upstream보다 **뒤처지지 않았는지** 확인한다(앞서 있는 것은 괜찮다). 뒤처졌으면 멈추고 묻는다.
3. `androidApp/google-services.json`이 있다(Firebase 앱 ID를 여기서 읽는다). Firebase 업로드는 이 맥에 로그인된 firebase CLI를 쓴다.
4. status lane 두 개를 0절처럼 차례로 실행한다.

### 4-2. 질문

1. **대상 플랫폼**: 1-1과 같다.
2. **빌드 타입**: 고른 플랫폼마다 하나씩, 한 번의 AskUserQuestion에 묻는다. 옵션은 `debug (권장)` / `release`.
   - Android debug: 디버그 키로 서명된다. 패키지 이름이 정식 앱과 같아서 Play에서 받은 앱이 있는 폰에는 설치되지 않는다(지우고 설치해야 한다). 문구에 이 점을 적는다.
   - Android release: 업로드 키로 서명한다. `local.properties`에 `RELEASE_*` 값이 있어야 한다.
   - iOS Debug: `DEBUG` 분기(테스트 광고 ID 등)가 들어간 빌드다. iOS Release는 정식 빌드와 같다.
3. **iOS 버전(필요할 때만)**: iOS를 고르고 리포의 `MARKETING_VERSION`이 닫혀 있을 때만 묻는다. 닫혀 있다는 것은 `app_store_live.version`과 같거나, `asc_latest_upload.version`보다 낮다는 뜻이다. 후보는 1-2의 계산을 그대로 쓴다(열려 있는 버전에 빌드만 추가 / patch / minor). 버전이 열려 있으면 묻지 않는다. Android 버전 이름은 바꾸지 않는다.
4. **노트**: 묻지 않고 만든다. 플랫폼마다 파일 하나씩 저장한다(`fastlane/output/qa-notes-android.txt`, `fastlane/output/qa-notes-ios.txt`).
   - 첫 줄: Android는 `[QA] <브랜치> @ <짧은 해시> (<빌드 타입>)`, iOS는 `<브랜치> @ <짧은 해시>`. iOS는 lane이 그 위에 `[QA DEBUG - 심사 제출 금지]`(Release면 `[QA RELEASE - ...]`) 줄을 자동으로 붙인다. QA 빌드는 정식 빌드와 같은 버전 아래에 쌓이므로, 이 줄이 App Store Connect에서 심사에 제출하면 안 되는 빌드임을 알려 준다.
   - 변경 요약: main이 아닌 브랜치면 `origin/main..HEAD`, main이면 그 플랫폼의 `last_tag..HEAD`의 커밋 제목을 최대 8개 "- "로 적는다. QA용이라 커밋 제목을 거의 그대로 써도 된다.
   - Android debug면 마지막 줄에 "Play에서 받은 척척학사가 있으면 지우고 설치하세요."를 넣는다.
   - 이모지와 `<` 문자는 쓰지 않는다.
5. **최종 확인**: 플랫폼별 빌드 타입, 예상 번호(Android는 Play 최대값+1과 리포 versionCode+1 중 큰 값, iOS는 ASC 최신 빌드+1), iOS 버전, 노트 전문(iOS는 lane이 붙일 첫 줄 포함), push할 브랜치를 적고 `진행` / `취소`를 묻는다.

### 4-3. 실행

1. iOS 버전을 정했으면 `bundle exec fastlane bump version:<V> platform:ios`.
2. 업로드:
   ```bash
   bundle exec fastlane android qa build_type:<debug|release> notes_file:fastlane/output/qa-notes-android.txt > fastlane/output/android-qa.log 2>&1
   bundle exec fastlane ios qa build_type:<debug|release> notes_file:fastlane/output/qa-notes-ios.txt > fastlane/output/ios-qa.log 2>&1
   ```
3. 성공하면 그 플랫폼의 버전 파일만 커밋한다. 태그는 만들지 않는다. 메시지 끝에는 세션의 attribution 줄을 붙인다.
   - Android: `chore(android): QA 빌드 <versionName> (versionCode <N>, <debug|release>)`
   - iOS: `chore(ios): QA 빌드 <V> (빌드 <N>, <Debug|Release>)`
4. 실패하면 2절 4번처럼 그 플랫폼의 버전 파일을 되돌리고 원인을 기록한다. 실패 표에 더해 다음을 본다.

   | 메시지 | 원인과 조치 |
   |---|---|
   | `firebase` 명령의 인증 오류(`Failed to authenticate`, `401`, `403`) | firebase CLI 로그인이 만료됐다. 사용자에게 별도 터미널에서 `firebase login --reauth`를 실행해 달라고 한다 |
   | `Missing .../google-services.json` | 새 worktree다. `.claude/scripts/setup-worktree.sh`로 연결한다 |

5. push: 커밋이 생겼으면 현재 브랜치를 push한다. upstream이 있으면 `git fetch origin` 후 upstream이 그대로인지 확인하고 `git push origin <브랜치>`, 없으면 `git push -u origin <브랜치>`. 그사이 upstream이 움직였으면 push하지 말고 묻는다. `--force`는 쓰지 않는다.

### 4-4. 보고

- 플랫폼별 결과: 빌드 타입, 버전, 번호, 성공 여부(실패면 원인)
- 사용한 노트, 만든 커밋, push한 브랜치
- 테스터 쪽 상황: Firebase는 '척척학사' 그룹에 메일이 바로 간다. TestFlight는 처리(보통 10~30분)가 끝나면 내부 테스터가 설치할 수 있다.
- Android debug를 올렸으면 "정식 앱을 지우고 설치해야 한다"는 점을 다시 적는다.

## 하지 않는 것

- 사용자가 요청하지 않으면 `dry_run`을 돌리지 않는다.
- 프로덕션 출시, 심사 제출, 콘솔의 약관 동의는 하지 않는다. QA 모드는 TestFlight 외부 그룹에 배포하지 않는다.
- 키 파일, `asc-api-key.env`, `local.properties`의 내용을 읽거나 출력하지 않는다.
- 이미 있는 태그를 옮기거나 지우지 않는다. 정식 모드는 main만, QA 모드는 현재 브랜치만 push한다.
