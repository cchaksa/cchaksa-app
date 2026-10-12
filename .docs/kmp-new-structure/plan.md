# KMP 새 기본 구조(AGP 9) 전환 계획

- 작성일: 2026-10-12
- 기준 커밋: `origin/main` 7d421687 (워크트리 `kmp-new-structure-plan`)
- 상태: 결정 완료(2026-10-12, D1~D5 모두 권장안 채택). 선행 조건 충족: Android Studio Rabbit 1 업그레이드, Room 작업 반영(612794fa).
  워크트리 기준 커밋은 612794fa다. 구현은 사용자가 시작을 지시할 때까지 보류한다(코드 변경 없음).
- **현재 위치: 단계 1(BuildConfig 분리) 시작 전.** 단계 0은 끝났다(4번 에뮬레이터 확인은 사람 QA가 맡으므로 생략). 0장 끝의
  "진행 현황과 다음 행동"에 단계별 상태를 적었다.
- QA: 실동작 확인은 사람이 QA로 한다. 에이전트는 에뮬레이터 체크리스트를 돌리지 않고, 각 단계의 완료 판정은 빌드·테스트·산출물
  비교로 한다. 부록 A.5는 PR에 첨부해 QA에 넘기는 체크리스트로 쓴다(2026-10-12 사용자 결정).
- 입력: [brief.md](./brief.md)
- 표기: 이 문서는 Android용 Firebase 설정 파일을 "`google-services` JSON"으로 부른다. 프로젝트 `.claude/settings.json`의 훅이 그
  파일 이름 문자열이 들어간 쓰기를 막기 때문이다.

---

## 0. 요약

**결론.** 빌드가 되는 상태를 유지하면서 다음 순서로 전환한다. 스크래치 사본에서 최종 상태(AGP 9.4.1, Gradle 9.7.1, Kotlin 2.4.21,
Android-KMP 라이브러리 플러그인, `shared` 이름 변경)까지 재현했고, 단계마다 Android 빌드, 호스트 테스트 71개, iOS 프레임워크 링크를
통과시켰다. APK의 매니페스트·네이티브 라이브러리·assets·리소스가 기준 상태와 같다는 점도 비교로 확인했다. 최종 상태에서는 `xcodebuild`도 성공했다.

| 단계 | 내용 | AGP | 예상 소요 |
|---|---|---|---|
| 0 | 준비: Room 작업 반영, Studio 업그레이드, 기준 산출물 기록 | 8.10.1 | 0.5시간 (+ Studio 설치) |
| 1 | BuildConfig 분리: 앱이 설정 객체를 넘기는 구조로 변경 | 8.10.1 | 1~1.5시간 |
| 2 | `androidApp` 분리, `composeApp`을 `com.android.library`로 변경 | 8.10.1 | 2~3시간 |
| 3 | AGP 9 전환: 툴체인 상향과 Android-KMP 라이브러리 플러그인 | 9.4.1 | 2~3시간 |
| 4 | `composeApp` → `shared` 이름 변경 (D2) | 9.4.1 | 1~1.5시간 |
| 5 | 문서·CI 정리 | – | 1시간 |

**메인 세션 제안에서 바꾼 점** (근거는 3장에 있다)

1. BuildConfig 분리를 맨 앞으로 옮긴다. 라이브러리 모듈은 AGP 8에서도 BuildConfig를 기본으로 생성하지 않고(`buildFeatures.buildConfig`
   기본값 false), 켜더라도 `VERSION_NAME`이 없다(AGP 4.1부터). 따라서 "AGP 8에서 앱을 분리하면 BuildConfig가 아직 동작한다"는 전제가
   성립하지 않는다.
2. Android-KMP 라이브러리 플러그인 전환과 AGP 9·Gradle 9·Kotlin 상향을 한 단계로 묶는다. AGP 8.10.1에서는 `kotlin { android {} }`
   DSL(AGP 8.12부터)과 `androidRuntimeClasspath` 프리뷰 의존성(AGP 9.0.0-beta01부터)을 쓸 수 없다. 버전만 AGP 8.13.2 위에서 먼저 올리는
   분할안도 시험했지만, D8이 Kotlin 2.4 메타데이터를 해석하지 못해 경고 5,762건을 내므로 채택하지 않았다.

**스파이크에서 드러난 필수 상향.** Firebase Crashlytics Gradle 플러그인 2.9.9 → 3.0.8, Room 2.7.1 → 2.8.5.

**조용히 지나갈 수 있는 회귀 4곳.** 각 단계의 완료 판정에 확인 절차를 넣었다.

- 공유 모듈에서 `androidResources`를 켜지 않으면 빌드는 성공하지만 APK의 Compose 리소스가 51개에서 0개가 된다.
- AGP 9 전환 뒤에는 루트의 `./gradlew testDebugUnitTest`가 공유 모듈 테스트를 하나도 실행하지 않는다. 공유 모듈의 테스트 태스크 이름이
  `testAndroidHostTest`로 바뀌기 때문이다.
- `HttpClientModule.kt`의 최상위 `BASE_URL`이 클래스 초기화 시점에 `isDebug`를 읽는다. 그래서 `isDebug`를 주입 방식으로 바꾸면 호스트
  테스트 7개가 실패한다.
- 모듈 이름을 바꾸면 Compose `Res` 클래스의 패키지가 바뀌어 컴파일 오류가 230건 난다. `packageOfResClass`로 기존 패키지를 고정해야 한다.

**Android Studio.** 계획 작성 당시 설치된 Panda 2(2025.3.2)는 AGP 9.1까지만 지원했다. AGP 9.4.1을 쓰려면 Quail 4(2026.1.4) 이상이
필요하며, 현재 안정판은 Rabbit 1(2026.2.1)이다. 2026-10-12에 Rabbit 1로 업그레이드한 것을 확인했다.

**AGP 10.** 아직 출시되지 않았다. Google Maven의 AGP 메타데이터(2026-10-08 갱신)에 10.x 버전이 하나도 없고, 공식 타임라인은
"AGP 10.0 (late 2026)"이다.

**결정** (4장, 2026-10-12 확정). 다섯 항목 모두 권장안을 채택했다.

- D1: AGP 9.4.1, Gradle 9.7.1, Android Studio Rabbit 1(2026.2.1)
- D2: `shared`로 이름을 바꾼다. 마지막에 단독 단계로 진행하고, `Res` 패키지는 `packageOfResClass`로 고정한다(B-1).
- D3: 앱 모듈의 BuildConfig로 `AndroidAppConfig`를 만들어 `initKoinAndroid`로 주입한다.
- D4: 메인 체크아웃의 Room 마이그레이션 작업을 먼저 마무리해 main에 커밋(반영)한 뒤 시작한다.
- D5: 실제로 존재하는 태스크만 실행하는 최소 CI로 교체한다.

**진행 현황과 다음 행동** (2026-10-12 확인)

> **현재 위치: 단계 1(BuildConfig 분리) 시작 전.** 단계 0이 끝났으므로 사용자가 시작을 지시하면 단계 1을 진행한다.
> 기준 산출물은 [baseline/README.md](./baseline/README.md)에 있다.

| 단계 | 상태 | 남은 일 |
|---|---|---|
| 0. 준비 | 완료(2026-10-12) | 1~3번을 마쳤다. 4번(에뮬레이터 기준 동작 기록)은 사람 QA가 맡으므로 생략했다 |
| 1. BuildConfig 분리 | **다음 차례 (현재 위치)** | 사용자가 시작을 지시하면 진행한다. 첫 PR이다 |
| 2. `androidApp` 분리 | 대기 | |
| 3. AGP 9 전환 | 대기 | |
| 4. `shared` 이름 변경 | 대기 | `composeApp/` 경로를 건드리는 다른 브랜치를 먼저 머지해야 한다 |
| 5. 문서·CI 정리 | 대기 | |

현재 상태를 확인한 결과는 다음과 같다.

- 워크트리 HEAD와 `origin/main`이 모두 612794fa다. 그 뒤로 main에 새로 올라온 커밋은 없다.
- 이 워크트리에 `local.properties`, `iosApp/Configuration/Config.xcconfig`, Firebase 설정 파일 두 개(Android·iOS)가 있어서 실제 설정으로
  빌드할 수 있다.
- 단계 0의 3번 기준 산출물을 2026-10-12에 기록했다([baseline/README.md](./baseline/README.md)). 클린 빌드에서 호스트 테스트 81개가 모두
  통과했고, debug·release APK는 `lib/` 8개(`libsqliteJni.so` 없음), Compose 리소스 51개, 리소스 이름 3,474개다. iOS 프레임워크 링크와
  `xcodebuild`(`JAVA_HOME` 덮어쓰기)도 성공했다. 매니페스트 요소 수는 스크립트로 세면 debug 156개, release 155개로, 3.5절의 값과 세는
  방법이 다르다. 이후 단계는 baseline의 값과 비교한다.
- 에이전트는 사용자 전역 설정의 `Read(**/build/**)` 거부 규칙 때문에 `build/` 아래의 APK를 읽지 못한다. APK 스냅샷(부록 A.4)은 사용자가
  `!` 접두로 직접 실행하고, 에이전트는 `.docs/` 아래에 저장된 결과만 읽는다.
- Xcode의 Java Path(`IDEApplicationwideBuildSettings`의 `JAVA_HOME`)는 여전히 Studio 내장 JBR(JDK 25)을 가리킨다. 단계 0~2의
  `xcodebuild`는 명령에서 `JAVA_HOME`을 JDK 17로 덮어쓰므로 진행을 막지 않는다.

| 순서 | 행동 | 담당 | 상태 |
|---|---|---|---|
| 1 | 메인 체크아웃의 Room 마이그레이션 작업을 마무리해 main에 커밋(반영)한다 | 사용자(또는 그 작업을 맡은 세션) | 완료(2026-10-12). 612794fa "fix: 로컬 DB 마이그레이션 실패로 앱이 동작하지 않는 문제 수정"이 `origin/main`에 올라갔고, 이 워크트리를 그 위로 fast-forward했다 |
| 2 | Android Studio를 Rabbit 1(2026.2.1)로 업그레이드한다 | 사용자 | 완료(2026-10-12). 빌드 AI-262.9437.185.2621.16467767, 내장 `lastStableBuildVersion = 9.4.0` |
| 3 | 단계 0의 3·4번(기준 산출물, 에뮬레이터 기준 동작)을 기록한다 | 구현 세션 | 3번 완료(2026-10-12, [baseline/README.md](./baseline/README.md)). 4번은 사람 QA가 맡으므로 생략했다 |
| 4 | Rabbit 1 업그레이드로 생긴 JDK 25 문제에 대비한다(3.5절) | 사용자 | 미처리(2026-10-12 확인). 명령줄 검증에는 필요 없고, 3단계 전에 Xcode·Studio에서 직접 빌드할 때만 필요하다 |
| 5 | 단계 1(BuildConfig 분리)을 시작한다 | 구현 세션 | **다음 차례.** 사용자가 지시하면 진행한다 |

---

## 1. 배경과 범위

### 1.1 공식 자료가 요구하는 것

- AGP 9.0부터는 `org.jetbrains.kotlin.multiplatform`을 `com.android.application`이나 `com.android.library`와 같은 모듈에 적용할 수 없다.
  Android 진입점(Activity, Application)을 별도 앱 모듈로 옮기고, 공유 모듈은 `com.android.kotlin.multiplatform.library` 플러그인으로
  바꿔야 한다.
- 모듈 이름을 `shared`로 맞추는 것은 강제 사항이 아니다. 권장 구조 문서는 기존 `composeApp`을 공통 코드 모듈로 그대로 재사용해도 된다고
  적고 있다.
- 새 라이브러리 플러그인은 variant가 하나뿐이며 BuildConfig, build type, product flavor를 지원하지 않는다. Android 리소스와 호스트
  테스트는 기본으로 꺼져 있어 `androidResources { enable = true }`와 `withHostTest {}`로 켜야 한다. `androidLibrary {}` 블록은
  AGP 9.1.0-alpha09부터 deprecated이므로 `kotlin { android {} }` 형태를 쓴다.
- AGP 9.0은 built-in Kotlin을 기본으로 켜므로 앱 모듈에서 `org.jetbrains.kotlin.android` 플러그인을 제거한다. 임시 해제 옵션인
  `android.builtInKotlin=false`와 `android.newDsl=false`는 AGP 10에서 제거될 예정이라 이번 계획에서는 쓰지 않는다.

### 1.2 이 프로젝트에 해당하는 구성

iOS도 Compose UI를 쓰므로(`iosMain/.../MainViewController.kt`) 블로그의 `sharedLogic`/`sharedUI` 분할은 해당하지 않는다. 공유 모듈
하나와 `androidApp`으로 구성한다.

```
:androidApp   com.android.application (built-in Kotlin)
              MainActivity, ChukChukHaksaApplication, 앱 매니페스트, 런처 아이콘·테마·앱 이름,
              google-services, Firebase Crashlytics, packaging 설정, 버전, build type별 키·광고 ID
:composeApp   kotlin.multiplatform + com.android.kotlin.multiplatform.library   (D2에 따라 :shared)
              commonMain / androidMain / iosMain 전부, Glance 위젯과 receiver 선언, actual 구현,
              프리뷰 28개, Room 스키마, iOS 프레임워크(baseName "ComposeApp")
iosApp/       Xcode 프로젝트. 이름을 바꿀 때만 빌드 단계의 Gradle 태스크 경로가 바뀐다.
```

### 1.3 범위 밖

- Compose Multiplatform 1.10.0 → 1.12.1 상향. 스파이크에서 1.10.0이 AGP 9·Kotlin 2.4.21과 함께 Android·iOS 모두 동작했다. 상향은 UI
  동작이 바뀔 수 있어 별도 작업으로 다룬다.
- `compose.runtime` 같은 `compose.*` 의존성 접근자 정리. CMP 1.10에서 deprecated 경고가 나지만 동작에는 영향이 없다.
- 위젯을 별도 모듈로 분리하는 작업, ktlint·detekt 도입(D5 참고).

### 1.4 gather-todo 참고 결과

사용자 요청에 따라 로컬의 gather-todo(`~/orca/projects/gather-todo`, main 0f00d0b)를 읽기 전용으로 확인했다.

- 버전은 AGP 8.13.2, Gradle 8.14.4, Kotlin 2.4.10, CMP 1.11.1이며 아직 AGP 9로 옮기지 않았다.
- 구조는 `core`, `server`, `composeApp:{app, domain, data, presentation:{shared, ui, widget}}`로 나눈 계층형 다중 모듈이다. 다만 진입점
  모듈 `:composeApp:app`이 `kotlinMultiplatform`과 `androidApplication`을 함께 적용하고(Android와 desktop 진입점), 라이브러리 모듈도
  `com.android.library`를 쓴다. 따라서 AGP 9의 새 기본 구조를 보여 주는 사례는 아니며, gather-todo도 같은 전환이 필요하다.
- 이 계획에 가져온 판단은 두 가지다.
  - 커밋 dff8751은 "홀더는 채우기를 잊어도 컴파일이 통과하고 첫 접근에서야 죽는다"는 이유로, DI 루트가 Application을 필수 인자로 받게
    바꿨다. 이 계획의 BuildConfig 대체 설계(D3-A)도 같은 원칙에 따라 설정 없이는 DI를 시작할 수 없게 만든다.
  - 같은 커밋은 Kakao `AuthCodeHandlerActivity` 경로에서 `MainActivity.onCreate`가 돌지 않으므로 초기화를 Application이 소유해야 한다고
    적고 있다. 이 프로젝트도 Application에서 Koin을 시작하며, 설정 설치도 Application에서 한다.
- 참고로 gather-todo의 AGP 8.13.2 + Kotlin 2.4.10 조합에서도 이번 스파이크에서 본 D8 메타데이터 경고가 날 가능성이 있다. 직접 빌드해
  확인하지는 않았다.

---

## 2. 버전 조사 (2026-10-12 기준)

기억에 의존하지 않고 각 저장소의 Maven 메타데이터와 공식 릴리스 노트를 직접 조회했다.

### 2.1 목표 버전 표

| 구성 요소 | 현재 | 최신 안정판 | 계획 목표 | 근거 |
|---|---|---|---|---|
| AGP | 8.10.1 | 9.4.1 (프리뷰 9.5.0-alpha09) | **9.4.1** (D1 대안 9.3.3, 9.1.1) | [AGP 릴리스 노트](https://developer.android.com/build/releases/gradle-plugin), [Google Maven 메타데이터](https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml) |
| Gradle | 8.11.1 | 9.8.1 (2026-10-07) | **9.7.1** | [Gradle 릴리스](https://gradle.org/releases/). AGP 9.4 최소 9.6.0, Kotlin 2.4.21 검증 상한 9.7.0 |
| Kotlin (KGP, Compose 컴파일러, serialization 플러그인) | 2.2.21 | 2.4.21 (2026-10-08) | **2.4.21** | [Kotlin 릴리스](https://kotlinlang.org/docs/releases.html), [KGP 호환 표](https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin). 2.2.21은 Gradle 8.14·AGP 8.11.1이 상한이라 상향이 필수다 |
| KSP | 2.2.21-2.0.4 | 2.3.12 | **2.3.12** | [KSP 릴리스](https://github.com/google/ksp/releases). 2.3.12의 최소 AGP는 8.12.0 |
| Room (런타임·컴파일러·Gradle 플러그인) | 2.7.1 | 2.8.5 | **2.8.5 (필수)** | 스파이크 S3, [Room 릴리스 노트](https://developer.android.com/jetpack/androidx/releases/room) |
| Firebase Crashlytics Gradle 플러그인 | 2.9.9 | 3.0.8 | **3.0.8 (필수)** | 스파이크 S3, [Crashlytics v3 안내](https://firebase.google.com/docs/crashlytics/upgrade-to-crashlytics-gradle-plugin-v3) |
| google-services 플러그인 | 4.4.2 | 4.5.0 (2026-06-16) | **4.5.0** (4.4.2도 동작) | 스파이크 E3, [Google Play services 릴리스](https://developers.google.com/android/guides/releases) |
| Compose Multiplatform | 1.10.0 | 1.12.1 | **1.10.0 유지** | 스파이크 S3, [CMP 호환성](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html) |
| JDK | 17 | – | 17 | AGP 9.x의 최소·기본값이 17 |
| Android Studio | Panda 2 (2025.3.2) → 2026-10-12 Rabbit 1로 업그레이드 | Rabbit 1 (2026.2.1) | **Rabbit 1** (D1, 완료) | [Studio·AGP 호환 표](https://developer.android.com/studio/releases) |

나머지 의존성(compileSdk·targetSdk 36, minSdk 28, kotlinx 계열, Koin, Ktor, Firebase BOM, GitLive, Glance, Kakao SDK, play-services-ads 등)은
바꾸지 않는다. 스파이크에서 이 버전들 그대로 컴파일·테스트·iOS 링크가 통과했다.

### 2.2 호환성 근거

AGP 버전별 최소 Gradle과 최소 Android Studio는 다음과 같다.

| AGP | 최소 Gradle | 필요한 Android Studio | 출시 |
|---|---|---|---|
| 9.0 | 9.1.0 | Otter 3 Feature Drop (2025.2.3) 이상 | 2026-01 |
| 9.1 | 9.3.1 | Panda 2 (2025.3.2) 이상 | 2026-03 |
| 9.2 | 9.4.1 | Panda 4 (2025.3.4) 이상 | 2026-04 |
| 9.3 | 9.5.0 | Quail 2 (2026.1.2) 이상 | 2026-07 |
| 9.4 | 9.6.0 | Quail 4 (2026.1.4) 이상 | 2026-09 |

- Kotlin 2.4.20~2.4.21의 공식 검증 범위는 Gradle 7.6.3~9.7.0, AGP 8.5.2~9.3.1이다. AGP 9.4.1은 이 범위 밖이지만, 스파이크에서 KGP가
  "검증되지 않은 AGP 버전" 경고 없이 빌드·테스트를 통과했다.
- AGP 9.0은 KGP 2.2.10 이상에 런타임으로 의존한다.
- KSP 2.3.x에는 AGP 9 관련 수정이 누적되어 있다. 2.3.5는 AGP 9.0의 KSP/KAPT 순환 의존, 2.3.6은 built-in Kotlin 감지, 2.3.10은
  built-in Kotlin의 R 클래스 해석과 Kotlin 2.4 모듈 이름 문제를 고쳤다. 2.3.10부터 KMP에서 `ksp(...)` 구성이 deprecated이지만, 이
  프로젝트는 타깃별 구성(`kspAndroid` 등)을 쓰므로 해당하지 않는다.
- Room 2.8.0은 minSdk를 23으로 올렸고(이 프로젝트는 28), Room Gradle 플러그인의 최소 AGP를 8.4로 올렸다. 2.8.5는 닫힌 DB에 대한
  suspend 쿼리와 무효화 추적 호출이 `IllegalStateException`을 던지도록 바뀌었다.
- D8/R8: AGP 8.13.2와 Kotlin 2.4.21 조합에서는 D8이 Kotlin 메타데이터를 해석하지 못한다는 경고가 5,762건 나왔다. AGP 9.1.1, 9.3.3,
  9.4.1 조합에서는 0건이었다(스파이크 S3-a, E3~E5). Kotlin 2.4는 AGP 9와 함께 올려야 한다.
- AGP 10: [API·DSL 타임라인](https://developer.android.com/build/releases/gradle-plugin-roadmap)은 "AGP 10.0 (late 2026)"으로 적고 있다.
  그때 `android.newDsl`·`android.builtInKotlin` 해제 옵션과 레거시 Variant API(`applicationVariants`, 레거시 `BaseVariant`의
  `buildConfigField()`·`resValue()` 같은 즉시 변경 메서드 등)가 제거된다. 앱 모듈의 `defaultConfig`·`buildTypes`에서 쓰는
  `buildConfigField`와 `manifestPlaceholders`는 새 DSL(`com.android.build.api.dsl.VariantDimension`)에 속하므로 여기에 해당하지 않는다
  (AGP 9.4.1 `gradle-api`에서 확인). 이 계획의 최종 상태는 제거 대상을 쓰지 않으므로 AGP 10이 나와도 같은 구조를 유지할 수 있다.

### 2.3 설치된 Android Studio 확인

- `/Applications/Android Studio.app`의 `product-info.json`은 `dataDirectoryName`이 `AndroidStudio2025.3.2`, 빌드가
  `AI-253.30387.90.2532.14935130`이다. 내장된 `plugins/android/lib/libagp-version.jar`의 `version.properties`에는 `buildVersion = 9.1.0`이
  들어 있다.
- 공식 호환 표에서 Panda 2(2025.3.2)의 지원 범위는 AGP 7.0~9.1이다. 따라서 이 Studio로는 **AGP 9.4.1 프로젝트를 동기화할 수 없다**.
  AGP 9.4.x는 Quail 4(2026.1.4)와 Rabbit 1(2026.2.1)이 지원한다.
- 명령줄 빌드는 Studio 버전과 무관하다. 스파이크는 모두 명령줄로 진행했고, Studio에서 직접 열어 동기화 오류를 재현하지는 않았다.
- 갱신(2026-10-12): Rabbit 1(2026.2.1, 빌드 `AI-262.9437.185.2621.16467767`, `dataDirectoryName` `AndroidStudio2026.2.1`)로
  업그레이드했다. 내장 `version.properties`는 `buildVersion = 9.5.0-dev`, `lastStableBuildVersion = 9.4.0`이다. 공식 표의 지원 범위가
  AGP 7.1~9.4이므로 현재 프로젝트(AGP 8.10.1)와 전환 후 프로젝트(AGP 9.4.1)를 모두 열 수 있다.

---

## 3. 스파이크

### 3.1 방법

- 이 워크트리의 커밋 7d421687을 `git archive`로 스크래치 디렉터리에 풀고, 그 사본에서만 작업했다. 워크트리의 코드는 바꾸지 않았으며,
  결과를 기록한 뒤 사본을 삭제했다.
- 비밀 값은 쓰지 않았다. `google-services` JSON, `GoogleService-Info.plist`, `Config.xcconfig`는 가짜 값으로 만들었다.
  `local.properties`는 두지 않거나(빈 문자열 폴백), 빌드 타입 분기를 확인할 때만 `FAKE_*` 값을 넣었다. 메인 체크아웃의 실제 비밀 파일은
  열지 않았다.
- 단계마다 사본에 커밋을 남기고 APK를 비교했다. 비교 항목은 병합 매니페스트의 요소·속성 집합(`apkanalyzer manifest print`),
  `lib/`·`assets/` 목록, 리소스 이름 목록(`aapt2 dump resources`), `aapt2 dump badging`이다. 스크립트는 부록 A.4에 있다.
- 환경은 macOS, JDK 17.0.16, Android SDK build-tools 36.0.0, Xcode 27.0이다.

### 3.2 확인한 사실

| 번호 | 상태 | 결과 |
|---|---|---|
| S0 | 기준 (AGP 8.10.1) | `testDebugUnitTest assembleDebug` 성공. 호스트 테스트 71개 통과. APK의 `lib/` 8개, `libsqliteJni.so` 없음, `assets/composeResources` 51개 |
| S1 | BuildConfig 분리 | 처음에는 `TokenAuthIntegrationTest` 7개가 `ExceptionInInitializerError`와 `NoClassDefFoundError: HttpClientModuleKt`로 실패했다. 테스트가 `configureBearerAuth`를 부르면서 같은 파일의 `BASE_URL` 초기화식이 `isDebug`를 읽었기 때문이다. `BASE_URL`을 getter로 바꾼 뒤 71개 통과 |
| S2 | `androidApp` 분리, `composeApp` → `com.android.library` (AGP 8.10.1) | 성공, 71개 통과. 병합 매니페스트의 요소 집합, `lib/`, `assets/`, 리소스 이름 3,473개가 S1과 같다(순서만 다르다). dex 파일만 1개 늘었다 |
| S3 | AGP 9.4.1, Gradle 9.7.1, Kotlin 2.4.21, KSP 2.3.12, Android-KMP 라이브러리 플러그인 | Crashlytics 2.9.9가 구성 단계에서 `Could not get unknown property 'applicationVariants'`(CrashlyticsPlugin.groovy:93)로 실패해 3.0.8로 올렸다. Room 2.7.1은 `:composeApp:kspAndroidMain`에서 "Schema import directory was not provided to the annotation processor"로 실패해 2.8.5로 올렸다. 그 뒤 빌드 성공, `testAndroidHostTest` 71개 통과, 스키마 JSON 변경 없음. APK는 S2와 매니페스트·`lib/`·`assets/`·badging이 같고, 리소스 이름은 Crashlytics 3이 넣는 `string/com.google.firebase.crashlytics.version_control_info` 1개만 늘었다. `linkDebugFrameworkIosSimulatorArm64` 성공(첫 실행 4분 52초, Kotlin/Native 도구 내려받기 포함) |
| S3-a | 구조는 S2 그대로 두고 AGP 8.13.2, Gradle 8.14.4, Kotlin 2.4.21, KSP 2.3.12, Room 2.8.5, Crashlytics 3.0.8로 상향 | 빌드, 71개 테스트, iOS 링크는 성공했다. 그러나 D8 Kotlin 메타데이터 경고가 5,762건 나왔다. 그래서 버전 상향을 AGP 9 전환과 분리하지 않는다 |
| E1 | 공유 모듈의 `androidResources` 끔 | 위젯 receiver가 공유 모듈 매니페스트에 있으면 `:androidApp:processDebugResources`가 `xml/timetable_widget_info not found`로 실패했다. 위젯 선언을 앱 모듈로 옮기고 다시 시험하면 빌드는 성공하지만 APK의 `assets/composeResources`가 51개에서 0개가 되었다 |
| E2 | 빌드 타입 분기 (가짜 값) | debug는 BuildConfig `DEBUG=true`, `ADMOB_INTERSTITIAL_AD_UNIT_ID="FAKE_UNIT_TEST"`, 매니페스트 `ca-app-pub-FAKE~TEST`. release는 `DEBUG=false`, `FAKE_UNIT_PROD`, `ca-app-pub-FAKE~PROD`. 두 빌드 모두 Kakao scheme placeholder가 치환되었다 |
| E3~E5 | AGP 9.4.1 / 9.3.3 / 9.1.1 × google-services 4.5.0 (Gradle 9.7.1) | 세 조합 모두 성공, 71개 통과, Compose 리소스 51개, `libsqliteJni.so` 없음, D8 경고 0건 |
| E6 | `composeApp` → `shared` 이름 변경 | `packageOfResClass` 없이 바꾸면 `Unresolved reference 'composeapp'` 컴파일 오류가 230건 났다. 패키지를 고정하자 성공했고 71개 통과. 옮긴 파일 440개가 100% rename으로 기록되었다. 실패한 빌드의 산출물이 남아 Compose assets가 두 벌 들어간 적이 있었는데, 빌드 디렉터리를 지우고 다시 빌드하자 51개 한 벌로 돌아왔다 |
| E7 | 이름 변경 상태의 `xcodebuild` (가짜 xcconfig) | `** BUILD SUCCEEDED **`(2분 16초). Xcode 빌드 단계가 `:shared:embedAndSignAppleFrameworkForXcode`를 실행했고, Swift 코드가 Kotlin 2.4.21 프레임워크와 연결되었다 |

이 밖에 확인한 사실은 다음과 같다.

- `kspAndroid` 구성 이름은 새 플러그인에서도 유효하다. `kspAndroidMain` 태스크가 생성된다.
- 새 DSL에는 `kotlin { android { lint {} } }`, `packaging {}`, `enableCoreLibraryDesugaring`이 있다(AGP 9.4.1 `gradle-api`의
  `KotlinMultiplatformAndroidLibraryTarget`). `lint { disable += "NullSafeMutableLiveData" }`를 그대로 옮길 수 있다.
- `"androidRuntimeClasspath"(compose.uiTooling)`가 구성 단계를 통과한다.
- 구성 캐시는 Gradle 9.7.1에서 두 번째 실행 때 "Configuration cache entry reused"로 재사용되었다.
- AGP 9 전환 뒤 루트 `./gradlew testDebugUnitTest --dry-run`은 `:androidApp:testDebugUnitTest`만 고른다. 공유 모듈의 호스트 테스트
  태스크는 `:composeApp:testAndroidHostTest`이고, 집계 태스크는 `testAndroid`, 전체 타깃 집계는 `allTests`다.
- Kotlin 2.4.21은 Apple Silicon 호스트에서 `iosX64Test`가 비활성이라는 경고를 1건 낸다. 동작에는 영향이 없다.
- CI가 호출하는 `ktlintCheck`와 `detekt` 태스크는 현재 빌드에도 없다("Task 'ktlintCheck' not found in root project"). 워크플로는 원격에
  없는 `develop` 브랜치의 PR에서만 실행되도록 되어 있다(원격 브랜치는 `main`, `poc`, `feature/restore-ios-widget`).

### 3.3 확인하지 못한 가정

1. Android Studio(Rabbit 1)에서 Gradle 동기화가 되고 `androidMain` 프리뷰 28개와 `MainActivity`의 `AppAndroidPreview`가 렌더링된다는 점.
   공식 문서상 Android-KMP 플러그인의 프리뷰 문제는 AGP 9.0.0-alpha13과 Studio Otter 2 Feature Drop Canary 3에서 수정되었다.
2. 에뮬레이터에서의 실제 동작. 스플래시와 홈 진입, Kakao 로그인 리다이렉트, 전면 광고, 위젯 추가·갱신, Firebase·Crashlytics 초기화,
   WebView 디버깅, Amplitude 이벤트를 확인해야 한다. 스파이크는 가짜 설정이라 실행하지 않았다. 이 확인은 단계마다 사람 QA가 맡는다.
3. 실제 비밀 파일로 빌드해도 결과가 같다는 점. 구조상 차이는 없다고 본다.
4. AGP 9.4.1이 Kotlin 2.4.21의 공식 검증 범위 밖이라서 생길 수 있는 문제. 스파이크에서는 드러나지 않았다.
5. Room 2.8.5의 "닫힌 DB 호출 시 예외" 변경이 앱이나 Room 마이그레이션 작업의 경로에 영향을 주지 않는다는 점.
6. 서명된 release AAB 생성과 Play 업로드. Studio "Generate Signed Bundle"의 대상 모듈과 출력 경로가 `androidApp`으로 바뀐다.
7. iOS 시뮬레이터·실기기 실행. 빌드까지만 확인했다.

### 3.4 브리프의 위험 요소별 결론

| 브리프 항목 | 결론 |
|---|---|
| 1. BuildConfig 미지원 | 공유 모듈의 사용처 6곳은 브리프와 같다(Application의 1곳은 앱 모듈로 간다). 앱 모듈 BuildConfig로 설정 객체를 만들어 넘기는 방식(D3-A)을 S1·E2로 확인했다. BuildKonfig는 Android build type과 연결되지 않아 광고 ID 분기 위험이 남는다(D3) |
| 2. 매니페스트 placeholder | Kakao `AuthCodeHandlerActivity`와 AdMob `APPLICATION_ID`는 앱 매니페스트로 옮긴다. E2에서 debug·release 치환을 확인했다. 위젯 receiver는 Glance 코드와 함께 공유 모듈 매니페스트에 두되, 라이브러리 namespace가 바뀌므로 FQCN으로 적는다 |
| 3. packaging 제외 설정 | 앱 모듈로 옮긴다. S2와 S3에서 APK의 `lib/` 목록이 기준과 같고 `libsqliteJni.so`가 없음을 확인했다. 완료 판정에 `lib/` 비교를 넣었다 |
| 4. Android 리소스 | 공유 모듈은 `androidResources { enable = true }`가 필수다. 끄면 위젯 XML 링크가 실패하고, 위젯을 옮기더라도 Compose 리소스가 APK에서 사라진다(E1). 런처 아이콘, 테마, `app_name`, 로고 배경색은 앱 모듈로 옮기고, `res/xml/timetable_widget_info.xml`은 공유 모듈에 남긴다 |
| 5. 프리뷰 툴링 | 공유 모듈은 `"androidRuntimeClasspath"(compose.uiTooling)`로 바꾼다. 구성은 통과했지만 IDE 렌더링은 확인하지 못했다. `MainActivity`의 프리뷰는 앱 모듈의 `debugImplementation(compose.uiTooling)`으로 처리한다 |
| 6. Gradle 플러그인 배치 | google-services와 Crashlytics는 앱 모듈로 옮긴다. Room 플러그인은 2.8.5가 필요하다. `kspAndroid`는 그대로 유효하다. `kotlin { android {} }`를 쓰고 lint 설정도 그 안으로 옮긴다 |
| 7. 버전 동시 상향 | Crashlytics 3.0.8과 Room 2.8.5가 필수임을 확인했다. Kotlin 2.2.21은 Gradle 9를 지원하지 않아 상향이 필수다. AGP 8.13 위에서 버전만 먼저 올리는 분할안은 D8 경고 때문에 배제했다 |
| 8. 모듈 이름 변경 | `packageOfResClass` 고정이 필요하다(고정하지 않으면 오류 230건). pbxproj는 빌드 단계 1줄을 바꾸고, 검색 경로의 `composeApp` 항목은 지워도 된다. `baseName`을 유지하므로 Swift 파일은 바뀌지 않는다. `git mv` 전에 `./gradlew clean`을 실행한다. E7에서 `xcodebuild`까지 확인했다 |
| 9. CI | 구조 변경 이전부터 동작하지 않는 상태다. 처리 방식은 D5에서 정한다 |

### 3.5 결정 이후 확인한 사실 (2026-10-12)

새 `main`(612794fa)의 워크트리에 실제 설정 파일을 두고 기준 상태를 한 번 측정했다. 구현 시작 전에 단계 0에서 다시 기록한다.

- 호스트 테스트는 81개다. Room 작업이 `TimetableMigrationTest` 10개를 더했고, 모두 통과한다. 이후 단계의 테스트 수 기준은 71이 아니라 81이다.
- debug APK는 `lib/` 8개(`libsqliteJni.so` 없음), `assets/composeResources` 51개, 리소스 이름 3,474개, 매니페스트 요소 148개다. release는
  매니페스트 요소가 147개다. debug와 release의 차이는 BuildConfig의 `DEBUG`·`BUILD_TYPE`·광고 단위 ID와, 매니페스트의 `debuggable`·
  디버그용 `PreviewActivity`·AdMob 앱 ID뿐이다.
- **Rabbit 1 업그레이드로 생긴 JDK 25 문제.** Rabbit 1의 내장 JBR은 JDK 25.0.3이다. Xcode 전역 설정(Settings → Locations → Custom
  Paths의 "Java Path", `IDEApplicationwideBuildSettings`)이 `JAVA_HOME`을 Studio 내장 JBR 경로로 지정하고 있어서, Xcode의
  "Compile Kotlin Framework" 단계가 Gradle 8.11.1을 JDK 25로 실행한다. Gradle 8.11은 JDK 25에서 실행할 수 없으므로(JDK 25 실행은
  Gradle 9.1.0부터 지원) 현재 `main`의 `xcodebuild`가 "What went wrong: 25.0.3"으로 실패했다. 명령에 `JAVA_HOME=/opt/homebrew/opt/openjdk@17`을
  덮어쓰면 성공했다.
- 같은 이유로 Studio의 Gradle 동기화도 실패할 가능성이 높다. 메인 체크아웃의 `.gradle/config.properties`가 `java.home`을 Studio 내장
  JBR로 지정하고, `.idea/gradle.xml`의 `gradleJvm`이 `#GRADLE_LOCAL_JAVA_HOME`이기 때문이다. IDE에서 직접 확인하지는 않았다.
- 영향과 대응은 다음과 같다.
  - 3단계 전(Gradle 8.11.1)에 Xcode나 Studio로 빌드하려면 Xcode의 Java Path와 Studio의 Gradle JDK(Settings → Build, Execution,
    Deployment → Build Tools → Gradle)를 JDK 17 또는 21(`/opt/homebrew/opt/openjdk@17`, `/opt/homebrew/opt/openjdk@21`)로 바꾼다.
    명령줄 빌드는 셸의 `JAVA_HOME`(JDK 17)을 쓰므로 영향이 없다.
  - 3단계에서 Gradle 9.7.1로 올리면 JDK 25에서도 Gradle이 실행된다. 3단계 완료 판정에 "`JAVA_HOME`을 덮어쓰지 않은 `xcodebuild` 성공"을
    추가한다.
- 단계 1의 진입점 함수 파일 이름은 `di/InitKoinAndroid.kt`로 정했다. `.android.kt` 접미사는 이 저장소에서 `actual` 구현에 쓰이는데,
  이 함수는 `actual`이 아닌 Android 전용 함수이기 때문이다.

---

## 4. 결정 항목 (2026-10-12 확정)

### D1. 목표 AGP 버전과 Android Studio

| 선택지 | 필요한 Studio | Gradle | Kotlin 2.4.21 검증 범위 | 장점 | 단점 |
|---|---|---|---|---|---|
| **A. AGP 9.4.1 (권장)** | Quail 4 이상. 현재 안정판 Rabbit 1(2026.2.1) | 9.7.1 | 범위 밖(상한 9.3.1) | 현재 최신 안정판이라 AGP 10 직전까지 다시 올릴 일이 적다. 스파이크의 모든 실험을 이 조합으로 했다 | Studio 업그레이드가 필요하다. 공식 검증 범위 밖이다 |
| B. AGP 9.3.3 | Quail 2 이상 | 9.7.1 | 범위 안 | 공식 검증 범위 안이다. 스파이크(E4)에서 빌드·테스트가 통과했다 | Studio 업그레이드는 똑같이 필요하고, 9.4로 다시 올릴 일이 남는다 |
| C. AGP 9.1.1 | 설치된 Panda 2 그대로 | 9.7.1 (최소 9.3.1) | 범위 안 | Studio를 바꾸지 않아도 된다. 스파이크(E5)에서 빌드·테스트가 통과했다 | 이미 세 단계 뒤처진 버전이다. AGP 10 이전에 다시 올려야 하고, 그때 Studio 업그레이드도 결국 필요하다 |

권장은 A다. A와 B는 Studio와 Gradle이 같고 `libs.versions.toml`의 `agp` 한 줄만 다르므로, 3단계에서 Kotlin 쪽 문제가 보이면 그
자리에서 B로 내릴 수 있다. C는 지금의 Studio 업그레이드를 미루는 대신 AGP 상향을 한 번 더 해야 한다.

**결정: A (2026-10-12).**

### D2. `composeApp` → `shared` 이름 변경

| 선택지 | 장점 | 단점 |
|---|---|---|
| A. `composeApp` 유지 | 변경이 없다. 진행 중인 브랜치, 문서, 로컬 설정의 경로가 그대로다. 공식 문서도 허용한다 | `androidApp` 분리 뒤에는 `composeApp`이 앱이 아닌 라이브러리라서 이름과 역할이 어긋난다. 예를 들어 `:composeApp:assembleDebug`는 더 이상 APK를 만들지 않는다 |
| **B. `shared`로 변경 (권장, 마지막 단독 단계)** | 공식 기본 구조, 문서, 프로젝트 마법사와 이름이 맞는다. 사람과 에이전트가 이름만 보고 모듈의 역할을 알 수 있다 | `Res` 패키지를 고정해야 하고, 그 결과 import에는 `composeapp`이 남는다. 진행 중인 브랜치와 경로 충돌이 생길 수 있다. `git log -- shared`에 2025-04~06의 옛 `shared` 모듈 커밋 3개가 섞여 나온다(파일별 `--follow`와 `blame`에는 영향 없음). CLAUDE.md, README, 로컬 IDE 실행 구성, `.claude/settings.local.json`의 경로를 고쳐야 한다 |

권장은 B다. 다만 AGP 9 전환이 안정된 뒤, `composeApp/` 경로를 건드리는 다른 브랜치를 모두 머지한 시점에 단독 단계로 진행한다. AGP 9
대응과는 무관하므로 미뤄도 된다. `Res` 패키지는 두 가지로 처리할 수 있다.

- B-1 (권장): `compose.resources { packageOfResClass = "chukchukhaksa.composeapp.generated.resources" }`로 고정한다. import 131줄을
  건드리지 않고, APK의 assets 경로도 그대로다.
- B-2: import를 `chukchukhaksa.shared.generated.resources`로 일괄 치환한다. 이름이 완전히 일치하지만 수십 개 파일의 import 줄이 바뀌어
  리뷰와 blame이 흐려진다.

**결정: B, `Res` 패키지는 B-1 방식 (2026-10-12).**

### D3. BuildConfig 대체 방식

| 선택지 | 방식 | 장점 | 단점 |
|---|---|---|---|
| **A. 앱 모듈 BuildConfig로 `AndroidAppConfig`를 만들어 주입 (권장)** | 앱 모듈이 자기 BuildConfig로 `AndroidAppConfig(isDebug, versionName, amplitudeApiKey, admobInterstitialAdUnitId)`를 만들어 `initKoinAndroid(application, config)`에 넘긴다. 이 함수만 Koin을 시작하고 값을 설치한다. `AndroidAdManager`와 `AndroidAnalyticsClient`는 Koin으로 값을 받고, 최상위 `isDebug`와 `getAppVersionName()`은 설치된 값을 읽는다 | build type 분기를 AGP가 계속 맡으므로 debug에는 테스트 광고 ID, release에는 운영 ID가 들어간다(E2). 시크릿은 지금처럼 `local.properties` → 앱 BuildConfig로만 흐른다. 서드파티 플러그인이 필요 없다. 설정 없이는 DI를 시작할 수 없어 설치 누락을 컴파일 단계에서 막는다(gather-todo dff8751과 같은 원칙). iOS가 이미 Swift 쪽에서 Info.plist 값을 공유 코드에 넘기는 구조와 대칭을 이룬다. commonMain의 `isDebug` API가 그대로라 호출부 10여 곳을 바꾸지 않는다 | 최상위 `isDebug`는 프로세스 전역 값이라 `initKoinAndroid` 이전에 읽으면 런타임 예외가 난다. 스파이크에서 이런 지점 1곳(`BASE_URL`)을 찾아 고쳤다. 호스트 테스트가 `isDebug`에 닿으면 설정 설치가 필요하다(현재 0곳) |
| B. BuildKonfig 같은 상수 생성 플러그인 | `commonMain`에 상수 객체를 생성하고, flavor를 `-Pbuildkonfig.flavor=release` 같은 Gradle 속성으로 고른다 | iOS와 Android가 같은 상수를 쓴다. 컴파일 타임 상수다 | 공유 모듈은 variant가 하나라 Android build type과 연결되지 않는다. release 빌드마다 속성을 빠뜨리지 않아야 하고, 빠뜨리면 브리프가 지적한 위험(debug와 release의 광고 ID가 뒤바뀌는 AdMob 정책 위반)이 그대로 생긴다. 한 번의 Gradle 호출로 debug와 release를 함께 빌드하면 한쪽 값이 틀린다. iOS는 이미 xcconfig 경로가 있어 값의 출처가 둘이 된다. 키가 생성 소스의 상수가 되어 iOS 프레임워크에도 들어간다. 서드파티 플러그인의 AGP 9·구성 캐시 호환성을 따로 검증해야 한다 |
| C. 매니페스트 `<meta-data>`와 런타임 조회 | 앱 모듈의 `manifestPlaceholders`(build type별)로 값을 넣고, 공유 모듈이 `PackageManager`로 읽는다. AdMob `APPLICATION_ID`와 같은 방식이다 | build type 분기를 유지하고, 앱이 값을 넘기는 코드가 필요 없다 | `Context`가 필요해 최상위 `isDebug`에는 결국 홀더가 필요하다. 키 이름 오타가 런타임에야 드러난다. 호스트 테스트(JVM)에는 `PackageManager`가 없다 |

권장은 A다. 코드는 부록 A.1에 있다. 시크릿을 소스 상수로 두지 않는다는 CLAUDE.md 규칙("Secrets and Build-Config IDs")도 그대로 지킨다.

**결정: A (2026-10-12).**

### D4. 메인 체크아웃의 Room 마이그레이션 작업과의 순서

| 선택지 | 설명 |
|---|---|
| **A. Room 작업을 먼저 반영 (권장)** | Room 작업을 main에 커밋해 반영한 뒤 이 전환을 그 위에서 시작한다. DB 스키마 변경은 사용자 데이터를 다루므로, 빌드 시스템 변경과 섞지 않아야 회귀 원인을 가리기 쉽다. 3단계의 Room 2.8.5 상향 검증에 Room 작업의 마이그레이션 테스트가 자연스럽게 포함된다 |
| B. 1·2단계와 병행 | 1·2단계는 Room 작업이 바꾼 파일과 겹치지 않는다(6장). 3단계를 시작하기 전에 Room 작업을 반영한다 |
| C. 전환 후 Room 작업을 rebase | Room 작업을 Room 2.8.5·Kotlin 2.4 위에서 다시 검증해야 하고, 이름을 바꿨다면 새 스키마 JSON의 경로도 옮겨야 한다. 권장하지 않는다 |

**결정: A (2026-10-12).**

### D5. CI 처리

현재 `.github/workflows/android-pull-request-ci.yml`은 원격에 없는 `develop` 브랜치 대상 PR에서만 실행되고, 존재하지 않는 `ktlintCheck`와
`detekt` 태스크를 호출한다. 즉 지금도 실질적으로 동작하지 않는다.

| 선택지 | 설명 |
|---|---|
| A. 이번에는 손대지 않음 | 사실만 기록하고 별도 작업으로 넘긴다 |
| **B. 동작하는 최소 CI로 교체 (권장)** | 5단계에서 `main` 대상 PR로 바꾸고, JDK 17 설정, 가짜 `google-services` JSON을 `androidApp/`에 만드는 단계, `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`(4단계 이후 이름 기준)를 넣는다. ktlint·detekt 단계는 지운다. iOS 빌드는 macOS 러너 비용 때문에 제외한다. `actions/checkout`·`setup-java`도 최신 메이저로 올린다 |
| C. B에 ktlint·detekt 도입까지 | Kotlin 에이전트 스킬의 호환성 표에 따르면 ktlint Gradle 플러그인은 `android.builtInKotlin=false` 우회가, detekt 2.0 미만은 `android.newDsl=false`와 `android.builtInKotlin=false` 우회가 필요하다. AGP 10에서 사라질 옵션에 기대게 되므로 별도 작업으로 분리하는 편이 낫다 |

**결정: B (2026-10-12).**

---

## 5. 단계별 실행 계획

공통 원칙은 다음과 같다.

- 단계마다 PR을 하나씩 만들고, 각 PR이 끝난 상태에서 빌드와 테스트가 통과해야 한다. PR 단위로 되돌릴 수 있다.
- 실동작 확인은 사람이 QA로 한다. 에이전트의 완료 판정은 빌드, 테스트, 산출물 비교(부록 A.4)까지다. 각 PR 설명에 부록 A.5
  체크리스트와 그 단계에서 특히 볼 항목을 적어 QA에 넘긴다.
- `local.properties`, `google-services` JSON, `iosApp/Configuration/Config.xcconfig`, `GoogleService-Info.plist`는 절대 커밋하지 않는다.

### 단계 0. 준비

**목표.** 비교 기준과 도구를 갖춘다.

1. (완료, 2026-10-12) D4에 따라 Room 마이그레이션 작업을 main에 커밋해 반영하고, 이 워크트리를 최신 main 위로 맞춘다.
2. (완료, 2026-10-12) D1에 따라 Android Studio를 Rabbit 1(2026.2.1)로 업그레이드한다. 새 Studio는 AGP 7.1~9.4를 지원하므로 전환 전
   프로젝트도 열 수 있다.
3. (완료, 2026-10-12) 현재 main에서 기준 산출물을 기록한다. 저장 위치는 `.docs/kmp-new-structure/baseline/`이다(`.docs/`는 추적하지
   않으므로 커밋되지 않는다). 결과는 [baseline/README.md](./baseline/README.md)에 있다.
   - `./gradlew testDebugUnitTest assembleDebug assembleRelease`의 테스트 수와 `composeApp-debug.apk`
   - 부록 A.4 스크립트로 만든 매니페스트, `lib/`, `assets/`, 리소스 이름 목록
   - `./gradlew linkDebugFrameworkIosSimulatorArm64`와 `xcodebuild` 성공 여부(`xcodebuild`는 `JAVA_HOME`을 JDK 17로 덮어쓴다. 3.5절)
4. (생략, 2026-10-12) 에뮬레이터에서 부록 A.5 체크리스트를 한 번 실행해 기준 동작을 기록한다. 실동작 확인은 사람 QA가 맡는다.

**완료 판정.** 기준 산출물이 저장되어 있다. (완료, 2026-10-12)
**되돌리기.** 해당 없음.
**예상 소요.** 0.5시간과 Studio 내려받기·설치 시간.

### 단계 1. BuildConfig 분리 (AGP 8.10.1, 모듈 구조 그대로)

**목표.** 공유 코드(androidMain)가 `BuildConfig`를 참조하지 않게 하고, Application이 설정 객체를 넘긴다. 동작은 바뀌지 않는다.

**파일 변경** (경로는 `composeApp/src/androidMain/kotlin/com/chukchukhaksa/mobile/` 기준, 코드는 부록 A.1)

| 파일 | 변경 |
|---|---|
| (신규) `common/kmp/AndroidAppConfig.kt` | `data class AndroidAppConfig`와 internal 설치·조회 함수. 설치 전에 읽으면 원인을 적은 `IllegalStateException`을 던진다 |
| (신규) `di/InitKoinAndroid.kt` | `fun initKoinAndroid(application: Application, appConfig: AndroidAppConfig)`. 값을 설치하고 `initKoin { androidContext(...); modules(module { single { appConfig } }) }`를 호출한다 |
| `common/kmp/isDebug.android.kt` | `actual val isDebug: Boolean get() = androidAppConfig.isDebug` |
| `common/kmp/AppVersionName.android.kt` | `androidAppConfig.versionName`을 반환 |
| `di/AndroidModule.kt` | `BuildConfig` import를 지우고, Amplitude 키와 광고 단위 ID를 `get<AndroidAppConfig>()`에서 받는다 |
| `common/ad/AndroidAdManager.kt` | 생성자에 `defaultAdUnitId: String`을 추가하고, 로그 문구의 "BuildConfig 미주입"을 고친다 |
| `common/designsystem/component/webview/CchWebView.android.kt`, `WebViewHolder.android.kt` | `BuildConfig.DEBUG` → `isDebug` |
| `ChukChukHaksaApplication.kt` | `BuildConfig`로 `AndroidAppConfig`를 만들어 `initKoinAndroid`를 호출한다. Napier 설정은 `BuildConfig.DEBUG`를 직접 쓴다 |
| `composeApp/src/commonMain/.../remote/di/HttpClientModule.kt` | `private val BASE_URL = ...` → `private val BASE_URL: String get() = ...` |
| `composeApp/src/commonMain/.../common/ad/AdManager.kt` | KDoc의 "BuildConfig / Info.plist" 문구를 "AndroidAppConfig / Info.plist"로 고친다(선택) |

**위험과 대응**

- 클래스 초기화 시점의 읽기: 최상위 프로퍼티 초기화식에서 `isDebug`를 읽으면 설치 전 접근이 된다. 스파이크에서 `BASE_URL` 1곳을 찾았고,
  `grep -rnE "^(private |internal )?val .* = .*isDebug" composeApp/src/commonMain`으로 다른 곳이 없음을 확인했다. 리뷰에서 같은 패턴이
  새로 생기지 않는지 본다.
- 설치 순서: `ChukChukHaksaApplication.onCreate`에서 `initKoinAndroid`보다 먼저 `isDebug`를 읽지 않는다. Napier 설정에 `BuildConfig.DEBUG`를
  직접 쓰는 이유다.
- ContentProvider는 `Application.onCreate`보다 먼저 실행된다. 지금은 공유 모듈이 직접 선언한 provider가 없다. 나중에 provider를 추가할 때
  `isDebug`를 읽지 않도록 주의한다.
- 이 단계에서는 BuildConfig 생성 설정을 바꾸지 않으므로 debug·release 값의 출처는 그대로다.

**완료 판정**

- `./gradlew testDebugUnitTest assembleDebug assembleRelease`가 성공하고, 테스트 수가 단계 0과 같다.
- `./gradlew linkDebugFrameworkIosSimulatorArm64`가 성공한다(commonMain을 바꿨기 때문이다).
- `grep -rn "BuildConfig" composeApp/src/androidMain --include='*.kt'`의 결과가 `ChukChukHaksaApplication.kt`와 `AndroidAppConfig.kt`의
  KDoc뿐이다.
- (QA에 넘김, 완료 판정에서 제외) debug 빌드에서 디버그 배지가 보이고, WebView가 `chrome://inspect`에 나타나며, API 호스트가
  `dev.api.cchaksa.com`이고, 광고 게이트에서 테스트 광고가 뜬다. PR 설명에 적는다.

**되돌리기.** 커밋 하나를 revert한다.
**예상 소요.** 1~1.5시간.

### 단계 2. `androidApp` 분리 (AGP 8.10.1)

**목표.** Android 진입점과 앱 설정을 `:androidApp`(`com.android.application` + `org.jetbrains.kotlin.android`)으로 옮기고, `:composeApp`을
`com.android.library`로 바꾼다. APK 구성은 바뀌지 않는다.

**파일 변경**

| 파일 | 변경 |
|---|---|
| `settings.gradle.kts` | `include(":androidApp")` 추가 |
| `gradle/libs.versions.toml` | `kotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }` 추가 |
| `build.gradle.kts`(루트) | `kotlinAndroid`, `room`, `firebaseCrashlytics`를 `apply false`로 추가 |
| (신규) `androidApp/build.gradle.kts` | 기존 `composeApp`의 `android {}`에서 앱 전용 설정을 옮긴다. `applicationId`, `targetSdk`, `versionCode`·`versionName`, `buildConfigField`와 `manifestPlaceholders`(build type별 포함), `packaging`(`libsqliteJni.so` 제외와 `useLegacyPackaging = false` 포함), 코어 라이브러리 디슈가링, lint, google-services·Crashlytics 플러그인, 의존성. 3단계 이후 모습은 부록 A.2 |
| (신규) `androidApp/src/main/AndroidManifest.xml` | `<application>`(이름, 아이콘, 라벨, 테마), `MainActivity`, Kakao `AuthCodeHandlerActivity`(`kakao${KAKAO_NATIVE_APP_KEY}`), AdMob `APPLICATION_ID`(`${ADMOB_APP_ID}`) |
| `git mv` (Kotlin) | `MainActivity.kt`, `ChukChukHaksaApplication.kt` → `androidApp/src/main/kotlin/com/chukchukhaksa/mobile/` |
| `git mv` (리소스) | `res/mipmap-*` 6개 디렉터리, `res/drawable/ic_launcher_background.xml`, `res/drawable-v24/ic_launcher_foreground.xml`, `res/values/{strings,theme,ic_logo_background}.xml`, `res/values-v35/theme.xml` → `androidApp/src/main/res/` |
| `git mv` (기타) | `composeApp/src/androidMain/ic_logo-playstore.png` → `androidApp/src/main/` |
| `composeApp/src/androidMain/AndroidManifest.xml` | 위젯 receiver만 남기고, 이름을 `com.chukchukhaksa.mobile.widget.TimetableWidgetReceiver`(FQCN)로 적는다 |
| `composeApp/build.gradle.kts` | `androidApplication` → `androidLibrary`. google-services·Crashlytics 플러그인과 `localProperties`를 지운다. namespace를 `com.chukchukhaksa.mobile.shared`로 바꾸고, `applicationId`, `targetSdk`, 버전, `buildConfigField`, `manifestPlaceholders`, `packaging`, `buildTypes`, 디슈가링, `buildConfig` 기능을 지운다 |
| `.gitignore` | `google-services` JSON의 무시 경로를 `/composeApp/`에서 `/androidApp/`으로 바꾼다 |
| `README.md` | Setup 절에 Firebase 설정 파일 안내를 새로 넣는다. Android는 `androidApp/` 아래의 `google-services` JSON, iOS는 `iosApp/iosApp/GoogleService-Info.plist`이고 둘 다 gitignore 대상이라고 적는다. 지금은 README·CLAUDE.md·`docs/` 어디에도 이 안내가 없고, 위치는 `.gitignore`에만 있다. 같은 PR에서 Project Structure 절의 "단일 `composeApp` 모듈 구조"와 Build 절의 `./gradlew assembleDebug`도 `androidApp` 기준으로 고친다 |
| 로컬(미추적) | 각 체크아웃에서 `composeApp/` 아래의 `google-services` JSON을 `androidApp/`으로 옮긴다. Studio 실행 구성의 모듈을 `androidApp`으로 바꾼다 |
| `CLAUDE.md` | 버전 올리기 안내와 시크릿 안내의 경로를 `androidApp/build.gradle.kts`로 고친다. 안내가 어긋나는 기간이 없도록 같은 PR에 넣는다 |

공유 모듈에 남는 것은 위젯 receiver 선언과 `res/xml/timetable_widget_info.xml`, Glance 코드, actual 구현, 프리뷰 28개, iOS 코드다.
`androidMain`의 Kotlin 파일 61개 중 2개만 옮긴다.

**위험과 대응**

- namespace 충돌: 앱과 라이브러리의 namespace가 같으면 R과 BuildConfig 클래스가 겹친다. 라이브러리를 `com.chukchukhaksa.mobile.shared`로
  바꾸고, Kotlin 패키지(`com.chukchukhaksa.mobile...`)는 그대로 둔다. Kotlin 코드에서 R을 쓰는 곳은 없다.
- 매니페스트의 상대 이름: 라이브러리 namespace가 바뀌면 `.widget.TimetableWidgetReceiver`가 다른 패키지로 해석된다. FQCN으로 적는다.
- packaging 누락: `libsqliteJni.so` 제외를 빠뜨려도 빌드는 성공하고 16KB 대응만 조용히 깨진다. APK의 `lib/` 목록을 기준과 비교해 잡는다.
- `google-services` JSON 위치: 파일이 없으면 google-services 플러그인이 빌드를 실패시키므로 조용히 지나가지는 않는다. 다른 기기와 CI에도
  옮겨 두어야 한다. README에 새 위치를 적고, PR 설명에는 "기존 체크아웃은 `composeApp/`의 파일을 `androidApp/`으로 옮길 것"을 적는다.
- 앱 모듈의 의존성: `composeApp`의 의존성은 `implementation`이라 앱 모듈에서 보이지 않는다. `MainActivity`와 Application이 직접 쓰는
  라이브러리(compose runtime·ui·preview, activity-compose, core-splashscreen, Kakao SDK, play-services-ads, firebase-common, Napier)를
  앱 모듈에 선언한다.
- release 산출물 위치: Studio "Generate Signed Bundle"의 대상 모듈과 출력 경로가 `androidApp/release/`로 바뀐다.

**완료 판정**

- `./gradlew testDebugUnitTest assembleDebug assembleRelease`가 성공한다. APK 경로는 `androidApp/build/outputs/apk/debug/androidApp-debug.apk`다.
- 단계 0 기준과 비교했을 때(부록 A.4) 매니페스트 요소 집합, `lib/`(`libsqliteJni.so` 없음), `assets/`, 리소스 이름이 같다.
- `./gradlew linkDebugFrameworkIosSimulatorArm64`와 `xcodebuild`가 성공한다. pbxproj는 바꾸지 않는다. 아직 Gradle 8.11.1이므로
  `xcodebuild`에는 `JAVA_HOME=/opt/homebrew/opt/openjdk@17`을 덮어써서 실행한다(3.5절).
- (QA에 넘김, 완료 판정에서 제외) 부록 A.5 체크리스트 전 항목. 특히 런처 아이콘·이름, Kakao 로그인 복귀, 광고 게이트, 위젯을 본다.

**되돌리기.** PR을 revert하고, 로컬의 `google-services` JSON을 `composeApp/`으로 되돌린 뒤 실행 구성을 복구한다.
**예상 소요.** 2~3시간.

### 단계 3. AGP 9 전환

**목표.** AGP 9.4.1, Gradle 9.7.1, Kotlin 2.4.21로 올리고 `:composeApp`을 Android-KMP 라이브러리 플러그인으로 바꾼다. 필요한 서드파티
상향을 함께 한다. 빌드 파일 변경은 커밋 하나로 묶는다. 버전 상향과 플러그인 전환은 따로 떼면 중간 상태가 빌드되지 않기 때문이다.

**파일 변경**

| 파일 | 변경 |
|---|---|
| `gradle/libs.versions.toml` | `agp` 9.4.1, `kotlin` 2.4.21, `ksp` 2.3.12, `androidx-room` 2.8.5, `firebaseCrashlytics` 3.0.8, `google-service` 4.5.0. `androidMultiplatformLibrary = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }`를 추가하고, 쓰지 않게 되는 `kotlinAndroid`·`androidLibrary` 별칭을 지운다 |
| `gradle/wrapper/*`, `gradlew`, `gradlew.bat` | `./gradlew wrapper --gradle-version 9.7.1`을 두 번 실행해 jar와 스크립트까지 갱신한다 |
| `build.gradle.kts`(루트) | `androidMultiplatformLibrary`를 `apply false`로 추가하고 `kotlinAndroid`·`androidLibrary`를 지운다 |
| `androidApp/build.gradle.kts` | `kotlinAndroid` 플러그인을 지운다(built-in Kotlin). `kotlin { compilerOptions { jvmTarget } }`는 그대로 둔다 |
| `composeApp/build.gradle.kts` | 부록 A.3. 플러그인을 교체하고, `androidTarget {}`과 `android {}`를 `kotlin { android { namespace, compileSdk, minSdk, compilerOptions, androidResources { enable = true }, withHostTest {}, lint {} } }`로 합친다. `debugImplementation(compose.uiTooling)` → `"androidRuntimeClasspath"(compose.uiTooling)`. `kspAndroid`는 그대로 둔다. `compose.resources { packageOfResClass = "chukchukhaksa.composeapp.generated.resources" }`를 추가한다. 지금의 기본값을 명시만 하는 무변경 설정이며, D2-B를 위한 선행 작업이다 |
| `gradle.properties` | 필수 변경은 없다. `iosX64Test` 경고를 숨기려면 `kotlin.native.ignoreDisabledTargets=true`를 추가한다(선택) |
| `CLAUDE.md` | 검증 명령의 `testDebugUnitTest`를 `:composeApp:testAndroidHostTest`로 바꾸고, 버전 목록을 갱신한다 |
| `README.md` | Tech Stack의 Kotlin·Room 버전과, Setup의 요구 사항(Android Studio Quail 4 이상, JDK, Xcode Java Path)을 갱신한다 |

**위험과 대응**

- Crashlytics 2.9.9는 구성 단계에서 실패한다(`applicationVariants`). 3.0.8로 올린다. 스파이크에서 확인했다.
- Room 2.7.1의 Gradle 플러그인은 새 Android 타깃의 KSP에 스키마 경로를 넘기지 못해 `kspAndroidMain`이 실패한다. 2.8.5로 올린다. 스키마
  JSON은 바뀌지 않았다. 2.8.5의 동작 변경(닫힌 DB 호출 시 예외)은 사람 QA가 DB 경로를 확인하도록 PR 설명에 적는다.
- 테스트가 조용히 빠지는 문제: 루트 `testDebugUnitTest`는 `:androidApp`의 빈 테스트만 실행한다. `:composeApp:testAndroidHostTest`를 쓰고,
  테스트 수가 기준과 같은지 확인한다. `withHostTest {}`가 없으면 commonTest가 Android에서 아예 실행되지 않는다(AGP 9.2부터 경고).
- Compose 리소스 누락: `androidResources { enable = true }`를 빠뜨리면 빌드는 성공해도 APK의 Compose 리소스가 사라진다.
  `assets/composeResources` 개수를 확인한다.
- Kotlin 검증 범위: AGP 9.4.1은 Kotlin 2.4.21의 공식 검증 범위 밖이다. 문제가 보이면 `agp`만 9.3.3으로 내린다(D1-B).
- 프리뷰: Studio에서 `androidMain` 프리뷰를 하나 이상 렌더링해 확인한다.
- 첫 빌드 시간: Gradle 9.7.1과 Kotlin/Native 2.4.21 도구를 처음 내려받으므로 첫 iOS 링크에 5분 안팎이 걸린다.
- `compose.*` 의존성 접근자의 deprecated 경고는 동작에 영향이 없고, 정리는 범위 밖이다.

**완료 판정**

- `./gradlew :composeApp:testAndroidHostTest :androidApp:assembleDebug :androidApp:assembleRelease`가 성공하고, 테스트 수가 기준과 같다.
- `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`와 `xcodebuild`가 성공한다. `xcodebuild`는 `JAVA_HOME`을 덮어쓰지 않고
  Xcode 설정 그대로(Rabbit 1 내장 JBR, JDK 25) 실행해서 성공해야 한다(3.5절). commonTest를 iOS에서도 돌리려면
  `./gradlew :composeApp:iosSimulatorArm64Test`를 추가로 실행한다.
- 단계 2 APK와 비교했을 때 매니페스트 요소 집합, `lib/`, `assets/`가 같고, 리소스 이름은
  `string/com.google.firebase.crashlytics.version_control_info`만 늘어난다.
- 같은 명령을 두 번째 실행하면 "Configuration cache entry reused"가 나온다.
- 새 Studio에서 Gradle 동기화가 되고, 프리뷰가 렌더링되며, `androidApp` 실행 구성으로 앱이 실행된다.
- (QA에 넘김, 완료 판정에서 제외) 부록 A.5 체크리스트 전 항목. 특히 DB 항목(6번)은 Room 2.8.5 변경 때문에 반드시 보도록 PR 설명에 적는다.

**되돌리기.** PR을 revert한다. Rabbit 1은 AGP 8.10도 지원하므로 Studio는 되돌리지 않아도 된다.
**예상 소요.** 2~3시간.

### 단계 4. `composeApp` → `shared` 이름 변경 (D2)

**목표.** 모듈 이름을 공식 기본 구조에 맞춘다. 옮기는 파일의 내용은 바꾸지 않는다.

**절차**

1. `composeApp/` 경로를 건드리는 다른 브랜치를 먼저 머지한다.
2. `./gradlew clean`으로 빌드 산출물을 지운다. `git mv`는 무시된 `build/` 디렉터리까지 함께 옮기므로, 지우지 않으면 옛 산출물이 섞일 수
   있다(스파이크에서 Compose assets가 두 벌 들어간 사례).
3. 커밋 1: `git mv composeApp shared`와 아래 표의 참조 갱신. 옮긴 파일 440여 개의 내용은 바꾸지 않으므로 100% rename으로 기록된다.
   참조 갱신을 같은 커밋에 넣어야 이 커밋도 빌드된다.
4. 커밋 2: 문서의 경로를 갱신한다.

**파일 변경**

| 파일 | 변경 |
|---|---|
| `settings.gradle.kts` | `include(":composeApp")` → `include(":shared")` |
| `androidApp/build.gradle.kts` | `projects.composeApp` → `projects.shared` |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | 빌드 단계의 `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` → `:shared:`. `FRAMEWORK_SEARCH_PATHS` 두 곳의 `$(SRCROOT)/../composeApp/build/xcode-frameworks/...` 항목은 지워도 된다(`shared/` 항목은 이미 있다) |
| 바꾸지 않는 것 | 프레임워크 `baseName = "ComposeApp"`이므로 `import ComposeApp`을 쓰는 Swift 파일 6개는 그대로다. Room `schemaDirectory("$projectDir/schemas")`는 디렉터리와 함께 옮겨진다. `Res` 패키지는 단계 3에서 고정했다 |
| `README.md`, `CLAUDE.md` | 커밋 2에서 `composeApp` 경로와 모듈 이름을 `shared`로 고친다 |
| 로컬(미추적) | Studio 실행 구성, `.idea/gradle.xml`(동기화 때 다시 생성), `.claude/settings.local.json`의 `:composeApp:` 권한 패턴, 메인 체크아웃의 `composeApp/release/` 같은 무시된 디렉터리 |

**위험과 대응**

- `Res` 패키지: 고정하지 않으면 컴파일 오류가 230건 난다(E6). 단계 3에서 미리 고정해 둔다.
- 진행 중인 브랜치: 머지할 때 Git의 디렉터리 rename 감지가 새 파일을 옮겨 주지만, 충돌을 직접 해결해야 할 수 있다.
- 이력: `git log -- shared`에 2025년의 옛 `shared` 모듈 커밋 3개가 함께 나온다. 파일별 `git log --follow`와 `git blame`에는 영향이 없다.

**완료 판정**

- `git show -M --summary HEAD`에서 옮긴 파일이 모두 `rename ... (100%)`로 나온다.
- `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`, `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`, `xcodebuild`가
  성공한다.
- 클린 빌드 APK의 `assets/composeResources/chukchukhaksa.composeapp.generated.resources/`가 51개 한 벌이다.

**되돌리기.** 해당 커밋을 revert한다.
**예상 소요.** 1~1.5시간.

### 단계 5. 문서·CI 정리

- 7장의 문서를 갱신한다(단계 2·3에서 고친 부분을 제외한 나머지).
- D5-B에 따라 CI 워크플로를 교체한다. `main` 대상 PR에서 실행하고, 가짜 `google-services` JSON을 `androidApp/`에 만든 뒤
  `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`를 실행한다. ktlint·detekt 단계는 지운다.

**완료 판정.** CLAUDE.md의 명령을 그대로 실행해 모두 성공한다. PR에서 새 워크플로가 실행되어 통과한다.
**예상 소요.** 1시간.

---

## 6. 메인 체크아웃의 Room 마이그레이션 작업과의 관계

메인 체크아웃(`/Users/jinwook/StudioProjects/cchaksa-kmp`, main)에는 다음 변경이 커밋되지 않은 채 있다(읽기 전용으로 확인).

> 갱신(2026-10-12): 아래 변경은 612794fa로 main에 반영되었다. 지금 메인 체크아웃에 남은 미추적 항목은 `.docs/`와 `composeApp/release/`뿐이다.
> 아래 표의 단계 3·4 항목(Room 2.8.5 재검증, 스키마 경로 이동)은 여전히 해당한다.

- 수정 12개: `MainViewModel.kt`, `data/openlecture/` 2개, `local/database/common/di/DatabaseModule.kt`, `local/database/openlecture/` 2개,
  `local/database/openmajor/database/OpenMajorDatabase.kt`, `local/database/timetable/migration/TimetableMigration.kt`,
  `local/datasource/openlecture/...`, iosMain의 DB factory 3개
- 추가: `composeApp/schemas/`의 `OpenLectureDatabase/3.json`, `TimetableDatabase/1.json`, `composeApp/src/commonTest/.../local/`
- 기타: `composeApp/release/`(무시되지 않은 미추적 디렉터리)

| 전환 단계 | 겹치는 지점 | 영향 |
|---|---|---|
| 1 | `MainViewModel.kt`가 `getAppVersionName()`을 쓴다 | 단계 1은 이 API를 유지하므로 충돌하지 않는다 |
| 1 | `DatabaseModule.kt`(Koin 모듈) | 단계 1은 이 파일을 바꾸지 않고, `initKoinAndroid`가 기존 `initKoin`을 그대로 부른다. 충돌하지 않는다 |
| 2 | DB factory | Room 작업은 iosMain factory만 바꾸고, 단계 2는 androidMain factory를 옮기지 않는다. 충돌하지 않는다 |
| 3 | `androidx-room` 2.7.1 → 2.8.5 | Room 작업의 마이그레이션과 테스트를 2.8.5에서 다시 검증해야 한다. 2.8.5의 닫힌 DB 예외 변경이 마이그레이션 경로와 만날 수 있다 |
| 3 | `commonTest/.../local/` 새 테스트 | Android에서는 `testAndroidHostTest`, iOS에서는 `iosSimulatorArm64Test`로 돈다. Room 마이그레이션의 `step()` 계약은 드라이버마다 실패 양상이 다르므로 두 플랫폼에서 모두 돌린다 |
| 4 | `composeApp/schemas/`의 새 JSON 2개 | 이름 변경으로 디렉터리가 옮겨진다. Room 작업이 반영되지 않은 상태라면 경로가 어긋난다 |

순서는 D4-A(Room 작업을 먼저 반영)로 확정했다(2026-10-12).

---

## 7. 작업 후 갱신할 문서

**CLAUDE.md**

| 절 | 갱신 내용 |
|---|---|
| Project Overview | "single `composeApp` module" 설명을 `androidApp`과 공유 모듈 구성으로 바꾼다 |
| Build System and Commands | APK 관련 명령에 `:androidApp:` 접두를 붙인다(`:androidApp:assembleDebug`, `:androidApp:installDebug`). 양 플랫폼 검증 명령을 `./gradlew :composeApp:testAndroidHostTest :androidApp:assembleDebug`, `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`, `xcodebuild`로 바꾼다(이름 변경 시 `:shared:`) |
| Testing Commands | `testDebugUnitTest` → `:composeApp:testAndroidHostTest`(또는 `allTests`). 계측 테스트는 새 플러그인에서 `withDeviceTest {}`가 필요하며 지금은 계측 테스트가 없다고 적는다 |
| Key Dependencies | AGP 9.4.1, Gradle 9.7.1(새 항목), Kotlin 2.4.21, KSP 2.3.12, Room 2.8.5, Crashlytics 플러그인 3.0.8, google-services 4.5.0 |
| Architecture / Database | 스키마 경로(이름 변경 시 `shared/schemas`) |
| Design System Location | 경로(이름 변경 시 `shared/...`) |
| Multiplatform Targets | Android 앱 모듈 `:androidApp`, 공유 모듈 namespace `com.chukchukhaksa.mobile.shared`, 필요한 Android Studio 버전(Quail 4 이상) |
| Current Project State | 버전 표기가 이미 어긋나 있다(문서는 3.2.1 / 57 / iOS build 4, 실제는 Android 3.2.2 / 58, iOS 3.2.3 / 6). 함께 바로잡는다 |
| Bumping the App Version | `versionCode`·`versionName`의 위치를 `androidApp/build.gradle.kts`로 |
| Secrets and Build-Config IDs | Android 값의 흐름을 `local.properties` → `androidApp/build.gradle.kts`의 BuildConfig → `AndroidAppConfig` → 공유 모듈로 설명하고, `google-services` JSON의 위치를 `androidApp/`으로 적는다 |
| Timetable Widgets | Android 위젯 선언이 공유 모듈 매니페스트에 있고, 공유 모듈의 `androidResources`가 켜져 있어야 한다는 점 |
| Development Notes | 코어 라이브러리 디슈가링이 앱 모듈 설정이라는 점 |

**그 밖의 파일**

- `README.md`: 지금 README에는 Firebase 설정 파일 안내가 없고, 몇몇 내용은 이미 현재와 어긋나 있다. 단계별로 다음을 고친다.

  | 절 | 갱신 내용 | 시점 |
  |---|---|---|
  | Setup | Firebase 설정 파일 안내를 새로 추가한다. Android는 `androidApp/` 아래의 `google-services` JSON, iOS는 `iosApp/iosApp/GoogleService-Info.plist`(둘 다 gitignore 대상) | 단계 2 |
  | Setup | 요구 사항을 추가한다. Android Studio Quail 4 이상(Rabbit 1 권장), 명령줄 JDK 17 이상, Xcode Java Path 주의(3.5절) | 단계 3 |
  | Setup › API Key | `local.properties` 예시에 `KAKAO_NATIVE_APP_KEY`만 있다. CLAUDE.md의 키 목록(Amplitude, AdMob 포함)과 맞춘다(선택) | 단계 5 |
  | Tech Stack | Kotlin 2.2 → 2.4, Room 2.7 → 2.8 | 단계 3 |
  | Project Structure | "단일 `composeApp` 모듈 구조"를 `androidApp`과 공유 모듈 구성으로 바꾼다. 이름 변경 뒤에는 `shared` | 단계 2, 4 |
  | Build | `./gradlew assembleDebug` → `./gradlew :androidApp:assembleDebug`. iOS 안내의 "`iosApp.xcworkspace` 열고 빌드"는 이미 틀렸다(CLAUDE.md 기준 워크스페이스는 비어 있고 `iosApp.xcodeproj`를 써야 한다). 함께 고친다 | 단계 2 |

- `.gitignore`: 단계 2에서 `google-services` JSON의 무시 경로를 고친다
- `.github/workflows/android-pull-request-ci.yml`: D5에 따른다

---

## 부록 A. 핵심 코드와 스크립트

스파이크 최종 상태에서 발췌했다.

### A.1 BuildConfig 대체 (D3-A)

```kotlin
// composeApp/src/androidMain/kotlin/com/chukchukhaksa/mobile/common/kmp/AndroidAppConfig.kt
package com.chukchukhaksa.mobile.common.kmp

/**
 * 앱 모듈이 자기 BuildConfig로 만들어 넘기는 Android 빌드 설정.
 * 공유 모듈은 단일 variant라 debug/release 값을 스스로 알 수 없다.
 */
data class AndroidAppConfig(
    val isDebug: Boolean,
    val versionName: String,
    val amplitudeApiKey: String,
    val admobInterstitialAdUnitId: String,
)

@Volatile
private var installedConfig: AndroidAppConfig? = null

internal fun installAndroidAppConfig(config: AndroidAppConfig) {
    installedConfig = config
}

internal val androidAppConfig: AndroidAppConfig
    get() = checkNotNull(installedConfig) {
        "AndroidAppConfig가 설치되지 않았습니다. Application.onCreate에서 initKoinAndroid()를 먼저 호출해야 합니다."
    }
```

```kotlin
// composeApp/src/androidMain/kotlin/com/chukchukhaksa/mobile/di/InitKoinAndroid.kt
/** Android 진입점. 설정 없이는 DI를 시작할 수 없도록 [AndroidAppConfig]를 필수 인자로 받는다. */
fun initKoinAndroid(application: Application, appConfig: AndroidAppConfig) {
    installAndroidAppConfig(appConfig)
    initKoin {
        androidContext(application)
        modules(module { single { appConfig } })
    }
}
```

```kotlin
// androidApp/src/main/kotlin/com/chukchukhaksa/mobile/ChukChukHaksaApplication.kt (onCreate)
super.onCreate()

if (BuildConfig.DEBUG) {
    Napier.base(DebugAntilog())
}

initKoinAndroid(
    application = this,
    appConfig = AndroidAppConfig(
        isDebug = BuildConfig.DEBUG,
        versionName = BuildConfig.VERSION_NAME,
        amplitudeApiKey = if (BuildConfig.DEBUG) BuildConfig.AMPLITUDE_API_KEY_DEV else BuildConfig.AMPLITUDE_API_KEY_PROD,
        admobInterstitialAdUnitId = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID,
    ),
)

Firebase.initialize(this)
KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
MobileAds.initialize(this)
```

```kotlin
// AndroidModule.kt (발췌)
single<AdManager>(createdAtStart = true) {
    AndroidAdManager(androidApplication(), get<AndroidAppConfig>().admobInterstitialAdUnitId)
}
single<AnalyticsClient> {
    AndroidAnalyticsClient(androidApplication(), get<AndroidAppConfig>().amplitudeApiKey)
}

// HttpClientModule.kt: 클래스 초기화 때 isDebug를 읽지 않도록 getter로 바꾼다.
private val BASE_URL: String
    get() = if (isDebug) "https://dev.api.cchaksa.com/api/" else "https://api.cchaksa.com/api/"
```

### A.2 `androidApp/build.gradle.kts` (단계 3 이후)

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().apply {
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
    load(localPropertiesFile.inputStream())
  }
}

plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.googleService)
  alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

android {
  namespace = "com.chukchukhaksa.mobile"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.kunize.uswtimetable"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 58
    versionName = "3.2.2"

    buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${localProperties["KAKAO_NATIVE_APP_KEY"] ?: ""}\"")
    manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = localProperties["KAKAO_NATIVE_APP_KEY"] ?: ""

    buildConfigField("String", "AMPLITUDE_API_KEY_DEV", "\"${localProperties["AMPLITUDE_API_KEY_DEV"] ?: ""}\"")
    buildConfigField("String", "AMPLITUDE_API_KEY_PROD", "\"${localProperties["AMPLITUDE_API_KEY_PROD"] ?: ""}\"")
  }
  packaging {
    jniLibs {
      useLegacyPackaging = false
      // Bundled SQLite is iOS-only; keep the prebuilt .so out of the Android APK.
      excludes += "**/libsqliteJni.so"
    }
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("debug") {
      manifestPlaceholders["ADMOB_APP_ID"] = localProperties["ADMOB_APP_ID_TEST"] ?: ""
      buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"${localProperties["ADMOB_INTERSTITIAL_AD_UNIT_ID_TEST"] ?: ""}\"")
    }
    getByName("release") {
      isMinifyEnabled = false
      manifestPlaceholders["ADMOB_APP_ID"] = localProperties["ADMOB_APP_ID_PROD"] ?: ""
      buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"${localProperties["ADMOB_INTERSTITIAL_AD_UNIT_ID_PROD"] ?: ""}\"")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }
  buildFeatures {
    buildConfig = true
  }
  lint {
    disable += "NullSafeMutableLiveData"
  }
}

dependencies {
  implementation(projects.composeApp)

  implementation(compose.runtime)
  implementation(compose.ui)
  implementation(compose.preview)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.kakao.sdk.v2.user)
  implementation(libs.play.services.ads)
  implementation(project.dependencies.platform(libs.firebase.bom))
  implementation(libs.firebase.common)
  implementation(libs.napier)

  coreLibraryDesugaring(libs.desugar.jdk.libs)
  debugImplementation(compose.uiTooling)
}
```

단계 2(AGP 8.10.1)에서는 `plugins`에 `alias(libs.plugins.kotlinAndroid)`가 하나 더 있다.

### A.3 `composeApp/build.gradle.kts`의 Android 부분 (단계 3 이후)

```kotlin
plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
}

kotlin {
  android {
    namespace = "com.chukchukhaksa.mobile.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
    androidResources {
      enable = true // 위젯 XML과 Compose 리소스 패키징에 필요하다(E1)
    }
    withHostTest {} // 없으면 commonTest가 Android에서 실행되지 않는다
    lint {
      disable += "NullSafeMutableLiveData"
    }
  }
  // iOS 타깃, sourceSets는 기존과 같다
}

compose.resources {
  // 모듈 이름과 무관하게 기존 Res 패키지를 유지한다.
  packageOfResClass = "chukchukhaksa.composeapp.generated.resources"
}

room {
  schemaDirectory("$projectDir/schemas")
}

dependencies {
  "androidRuntimeClasspath"(compose.uiTooling)
  kspKmp(libs.androidx.room.compiler) // kspAndroid, kspIosX64, kspIosArm64, kspIosSimulatorArm64
}
```

### A.4 APK 비교 스크립트

```bash
# apk-snapshot.sh <apk> <outdir>
APK=$1; OUT=$2
BT=$ANDROID_HOME/build-tools/36.0.0
AN=$ANDROID_HOME/cmdline-tools/latest/bin/apkanalyzer
mkdir -p "$OUT"
unzip -l "$APK" | awk 'NR>3 {print $4}' | grep -E '^lib/'    | sort > "$OUT/libs.txt"
unzip -l "$APK" | awk 'NR>3 {print $4}' | grep -E '^assets/' | sort > "$OUT/assets.txt"
"$BT/aapt2" dump resources "$APK" | grep -E '^\s+resource 0x7f' | awk '{print $3}' | sort > "$OUT/res-names.txt"
"$BT/aapt2" dump badging "$APK" | grep -E '^(package|application-label|launchable|uses-permission|sdkVersion|targetSdkVersion)' > "$OUT/badging.txt"
"$AN" manifest print "$APK" > "$OUT/manifest.xml"
```

```python
# manifest_diff.py <before.xml> <after.xml>: 순서를 무시하고 요소·속성 집합을 비교한다
import sys, xml.etree.ElementTree as ET
A = '{http://schemas.android.com/apk/res/android}'
def records(path):
    out = set()
    def walk(e, parent):
        attrs = tuple(sorted((k.replace(A, 'android:'), v) for k, v in e.attrib.items()))
        out.add((parent, e.tag, attrs))
        name = e.attrib.get(A + 'name', '')
        for c in e:
            walk(c, f"{parent}/{e.tag}" + (f"[{name}]" if name else ""))
    walk(ET.parse(path).getroot(), '')
    return out
a, b = records(sys.argv[1]), records(sys.argv[2])
print("only in before:", *sorted(a - b), sep="\n  ")
print("only in after:", *sorted(b - a), sep="\n  ")
```

실행용 사본은 `tools/apk-snapshot.sh`와 `tools/manifest_diff.py`에 있다. 사본은 위 스크립트와 두 가지가 다르다. 매니페스트에 치환된
Kakao 키와 AdMob 앱 ID를 해시 토큰(`kakaoREDACTED-xxxxxxxx` 등)으로 바꿔 저장하고, 개수를 `summary.txt`로 요약한다. 에이전트는 `build/`
아래를 읽지 못하므로 사용자가 `! bash .docs/kmp-new-structure/tools/apk-snapshot.sh <apk> <outdir>`로 실행한다.

판정 기준은 다음과 같다. `libs.txt`와 `assets.txt`는 `diff`가 비어야 한다(`libsqliteJni.so` 없음, `assets/composeResources` 51개).
`manifest_diff.py`는 양쪽 모두 비어야 한다. `res-names.txt`는 단계 3에서만 Crashlytics의 `version_control_info` 1줄이 늘어난다.

### A.5 QA 스모크 테스트 체크리스트

에이전트가 실행하지 않는다. 각 단계의 PR 설명에 붙여 사람 QA에 넘긴다.

1. 설치와 실행: `./gradlew :androidApp:installDebug`(단계 2 이전에는 `installDebug`). 런처 아이콘과 이름("척척학사")을 확인하고,
   스플래시를 지나 랜딩 또는 홈으로 들어간다.
2. 로그인: Kakao 로그인 후 `AuthCodeHandlerActivity`로 돌아와 홈에 진입한다.
3. WebView: 홈 탭의 웹뷰가 로드된다. debug 빌드에서는 디버그 배지가 보이고 `chrome://inspect`에 WebView가 나타나며, User-Agent가
   `Android/3.2.2/debug`다.
4. 광고 게이트: 광고가 걸린 경로에서 확인 다이얼로그 → 테스트 전면 광고 → 닫으면 이동한다.
5. 위젯: 홈 화면에 시간표 위젯을 추가하면 시간표가 표시되고, 앱에서 시간표를 바꾸면 위젯이 갱신된다.
6. DB: 시간표를 편집·저장하고 앱을 다시 시작해도 유지된다. 단계 3 이후에는 Room 2.8.5 변경 때문에 반드시 확인한다.
7. 로그: logcat에 FirebaseApp 초기화 오류, Crashlytics 오류, "AndroidAppConfig가 설치되지 않았습니다"가 없다. Napier HTTP 로그의 API
   호스트가 debug에서 `dev.api.cchaksa.com`이다.

---

## 부록 B. 출처

- JetBrains 블로그, New KMP default structure: https://blog.jetbrains.com/kotlin/2026/05/new-kmp-default-structure/
- AGP 9 마이그레이션 가이드(KMP): https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html
- 권장 프로젝트 구조: https://kotlinlang.org/docs/multiplatform/multiplatform-project-recommended-structure.html
- Android-KMP 라이브러리 플러그인: https://developer.android.com/kotlin/multiplatform/plugin
- Kotlin AGP 9 마이그레이션 에이전트 스킬: https://github.com/Kotlin/kotlin-agent-skills/blob/main/skills/kotlin-tooling-agp9-migration/SKILL.md
- AGP 릴리스 노트: [9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes),
  [9.3](https://developer.android.com/build/releases/agp-9-3-0-release-notes),
  [9.2](https://developer.android.com/build/releases/agp-9-2-0-release-notes),
  [9.1](https://developer.android.com/build/releases/agp-9-1-0-release-notes),
  [9.0](https://developer.android.com/build/releases/agp-9-0-0-release-notes),
  [4.1 (라이브러리 BuildConfig의 VERSION_NAME 제거)](https://developer.android.com/build/releases/agp-4-1-0-release-notes)
- AGP API·DSL 타임라인(AGP 10): https://developer.android.com/build/releases/gradle-plugin-roadmap
- Android Studio 릴리스와 AGP 호환 표: https://developer.android.com/studio/releases
- Google Maven 메타데이터: [AGP](https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml),
  [Crashlytics Gradle](https://dl.google.com/dl/android/maven2/com/google/firebase/firebase-crashlytics-gradle/maven-metadata.xml),
  [google-services](https://dl.google.com/dl/android/maven2/com/google/gms/google-services/maven-metadata.xml),
  [Room Gradle 플러그인](https://dl.google.com/dl/android/maven2/androidx/room/room-gradle-plugin/maven-metadata.xml)
- Maven Central 메타데이터: [KGP](https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/maven-metadata.xml),
  [KSP](https://repo1.maven.org/maven2/com/google/devtools/ksp/com.google.devtools.ksp.gradle.plugin/maven-metadata.xml),
  [Compose Multiplatform](https://repo1.maven.org/maven2/org/jetbrains/compose/compose-gradle-plugin/maven-metadata.xml)
- Gradle 릴리스: https://gradle.org/releases/ , 현재 버전 API: https://services.gradle.org/versions/current
- Kotlin 릴리스: https://kotlinlang.org/docs/releases.html , KGP 호환 표: https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin
- KSP 릴리스: https://github.com/google/ksp/releases
- Compose Multiplatform 호환성: https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html ,
  리소스 설정: https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html
- Room 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/room , Room KMP 설정: https://developer.android.com/kotlin/multiplatform/room
- Crashlytics Gradle 플러그인 v3: https://firebase.google.com/docs/crashlytics/upgrade-to-crashlytics-gradle-plugin-v3
- Google Play services·Gradle 플러그인 릴리스: https://developers.google.com/android/guides/releases
