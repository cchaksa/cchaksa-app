# KMP 새 기본 구조 전환 계획 수립 브리프

## 목표

JetBrains가 발표한 KMP 새 기본 프로젝트 구조(https://blog.jetbrains.com/kotlin/2026/05/new-kmp-default-structure/)와
AGP 9 대응을 이 프로젝트에 적용하기 위한 **실행 가능한 전환 계획**을 세운다. 이번 작업의 산출물은 구현이 아니라
계획 문서다.

- 산출물: `.docs/kmp-new-structure/plan.md` (이 브리프와 같은 폴더)
- 커밋, push, PR 생성은 하지 않는다. 사용자가 계획을 검토한 뒤 다음 단계를 정한다.

## 참고 자료 (1차 출처를 직접 확인할 것)

- JetBrains 블로그: https://blog.jetbrains.com/kotlin/2026/05/new-kmp-default-structure/
- AGP 9 마이그레이션 가이드: https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html
- Android-KMP 라이브러리 플러그인: https://developer.android.com/kotlin/multiplatform/plugin
- 오늘 날짜는 2026-10-12다. AGP·Gradle·Kotlin·KSP·Compose Multiplatform·google-services·Firebase Crashlytics·
  Room Gradle 플러그인의 **현재 안정 버전과 상호 호환성**, 그리고 **AGP 10 출시 여부**를 공식 릴리스 노트로 확인한다.
  기억에 의존하지 않는다.

## 현재 상태 (메인 세션에서 확인한 사실)

- 모듈은 `:composeApp` 하나다. `kotlin.multiplatform` + `com.android.application`을 함께 적용한다.
- 버전: AGP 8.10.1, Gradle 8.11.1, Kotlin 2.2.21, KSP 2.2.21-2.0.4, Compose Multiplatform 1.10.0.
- `androidMain`에는 Kotlin 파일이 약 60개 있다. 앱 모듈로 옮겨야 하는 진입점은 `MainActivity.kt`,
  `ChukChukHaksaApplication.kt` 두 개이고, 나머지(actual 구현, Glance 위젯, 프리뷰 28개)는 공유 모듈에 남는다.
- iOS는 Compose UI를 쓴다(`iosMain/.../MainViewController.kt`). 따라서 블로그의 `sharedLogic`/`sharedUI` 분할이
  아니라 `shared` 단일 모듈 구성이 해당한다.

## 메인 세션에서 짚은 위험 요소 (계획에서 검증하고 구체화할 것)

1. **BuildConfig 미지원**: 새 라이브러리 플러그인은 BuildConfig와 build type을 지원하지 않는다. Application이
   앱 모듈로 옮겨간 뒤에도 공유 모듈에 BuildConfig 사용처가 6곳 남는다.
   - `common/kmp/isDebug.android.kt` (`actual val isDebug = BuildConfig.DEBUG`, 최상위 값)
   - `common/kmp/AppVersionName.android.kt` (`BuildConfig.VERSION_NAME`)
   - `di/AndroidModule.kt` (Amplitude DEV/PROD 키)
   - `common/ad/AndroidAdManager.kt` (`ADMOB_INTERSTITIAL_AD_UNIT_ID`)
   - `common/designsystem/component/webview/CchWebView.android.kt`, `WebViewHolder.android.kt` (`BuildConfig.DEBUG`)
   - 광고 단위 ID의 debug/release 분기가 어긋나면 debug 빌드에서 실제 광고 ID가 노출되어 AdMob 정책 위반이 될 수
     있다. 메인 세션의 제안은 "앱 모듈에서 BuildConfig 값으로 설정 객체를 만들어 Koin으로 주입"이다.
     BuildKonfig 등 대안과 비교해 결정 근거를 남긴다. 시크릿은 소스에 상수로 두지 않는다는 프로젝트 규칙
     (CLAUDE.md "Secrets and Build-Config IDs")을 지켜야 한다.
2. **매니페스트 placeholder**: Kakao `AuthCodeHandlerActivity`(`kakao${KAKAO_NATIVE_APP_KEY}`)와 AdMob
   `APPLICATION_ID`(`${ADMOB_APP_ID}`)는 앱 모듈 매니페스트로 옮겨야 한다. 위젯 receiver를 어디에 둘지도 정한다.
3. **packaging 제외 설정**: `composeApp/build.gradle.kts`의 `excludes += "**/libsqliteJni.so"`는 최근 16KB 페이지
   대응 커밋(b4da6720)에서 추가한 설정이다. 앱 모듈로 옮길 때 빠뜨리면 회귀가 조용히 발생한다.
4. **Android 리소스**: 새 플러그인은 기본적으로 Android 리소스를 끈다. 위젯 xml(`res/xml/timetable_widget_info.xml`)과
   Compose 리소스 때문에 `androidResources { enable = true }`가 필요한지, 런처 아이콘·테마·strings는 어느 모듈로
   가는지 정한다.
5. **프리뷰 툴링**: `debugImplementation(compose.uiTooling)`을 `androidRuntimeClasspath(...)`로 바꿔야 하며,
   AGP 9.0.0-beta01 이상에서만 동작한다.
6. **Gradle 플러그인 배치**: google-services, Firebase Crashlytics는 앱 모듈로 가야 한다. Room 플러그인,
   `kspAndroid` 구성 이름이 새 플러그인에서도 유효한지 확인한다. `androidLibrary {}` 블록은 AGP 9.1.0-alpha09부터
   deprecated이므로 `kotlin { android { } }` 형태를 기준으로 삼는다.
7. **버전 동시 상향**: AGP 9, Gradle 9.1 이상, Kotlin과 KSP를 함께 올려야 한다. 서드파티 Gradle 플러그인 호환성이
   일정상 가장 불확실한 부분이다.
8. **모듈 이름 변경(`composeApp` → `shared`)**: 블로그는 기존 프로젝트에 이름까지 맞추는 것을 강제하지 않는다.
   바꿀 경우 `src` 밖에서 영향을 받는 추적 파일은 `settings.gradle.kts`, `iosApp/iosApp.xcodeproj/project.pbxproj`
   (빌드 단계의 `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`), `.gitignore`, `CLAUDE.md`, `README.md`다.
   pbxproj의 framework search path에는 이미 `$(SRCROOT)/../shared/build/xcode-frameworks/...`가 들어 있다.
   framework `baseName = "ComposeApp"`은 모듈 이름과 별개이므로, 유지하면 `import ComposeApp`을 쓰는 Swift 파일
   6개를 수정하지 않아도 된다. Room 스키마는 `$projectDir/schemas` 기준이다.
9. **CI**: `.github/workflows/android-pull-request-ci.yml`이 `ktlintCheck`, `detekt`를 호출한다. 루트 빌드에 해당
   플러그인이 보이지 않으므로 현재 상태부터 확인하고, 구조 변경 후에도 동작하게 한다.

## 메인 세션이 제안한 진행 순서 (검증 후 수정 가능)

1. AGP 8 위에서 `androidApp` 모듈을 분리하고 `composeApp`을 `com.android.library`로 바꾼다.
   이 단계에서는 BuildConfig가 아직 동작하므로 위험이 낮다.
2. Android-KMP 라이브러리 플러그인으로 전환하고 BuildConfig를 주입 방식으로 바꾼다.
3. AGP 9 / Gradle 9로 올린다.
4. `composeApp` → `shared` 이름 변경을 `git mv` 단독 커밋으로 진행한다. 리뷰와 `git blame` 추적을 위해 구조 변경과
   섞지 않는다.

각 단계는 끝났을 때 빌드가 되는 상태여야 한다.

## 작업 환경 제약

- 이 워크트리는 `origin/main`(7d421687) 기준이다. 메인 체크아웃(`/Users/jinwook/StudioProjects/cchaksa-kmp`)에는
  **커밋되지 않은 Room DB 마이그레이션 작업**이 있고, 이 워크트리에는 포함되어 있지 않다. 수정된 파일은 다음과 같다.
  - `MainViewModel.kt`, `data/openlecture/...`, `local/database/common/di/DatabaseModule.kt`,
    `local/database/openlecture/...`, `local/database/openmajor/...`, `local/database/timetable/migration/TimetableMigration.kt`,
    `local/datasource/openlecture/...`, iosMain의 DB factory 3개, 새 스키마 JSON 2개, `commonTest/.../local/`
  - 계획에는 이 작업과의 선후 관계(먼저 머지한 뒤 시작할지)와 충돌 가능 지점을 적는다.
- `local.properties`와 `iosApp/Configuration/Config.xcconfig`는 git에 추적되지 않아 이 워크트리에 없다. 현재 상태를
  빌드해서 확인해야 하면 메인 체크아웃에서 복사해 쓴다. 이 파일들은 절대 커밋하지 않는다.
- 위험한 가정을 확인하려는 짧은 스파이크(예: AGP 9 상향 시도)는 허용한다. 끝나면 코드 변경을 되돌리고, 결과만
  계획 문서에 기록한다.
- 검증 명령은 CLAUDE.md를 따른다: `./gradlew testDebugUnitTest assembleDebug`,
  `./gradlew linkDebugFrameworkIosSimulatorArm64`, `iosApp`의 `xcodebuild -project iosApp.xcodeproj -scheme iosApp ...`.
  실동작 확인은 USB 실기기가 아니라 Android 에뮬레이터에서 한다.

## plan.md에 담을 내용

- 단계별로: 목표, 파일 단위 변경 목록, 위험 요소와 대응, 완료 판정 기준(실행할 검증 명령), 되돌리는 방법, 예상 소요 시간
- 확정된 목표 버전 표(근거가 된 릴리스 노트 링크 포함)
- 사용자가 결정해야 할 항목과 각 선택지의 장단점. 최소한 다음은 포함한다.
  - `shared`로 이름을 바꿀지 여부
  - BuildConfig 대체 방식(Koin 주입 vs BuildKonfig 등)
  - 목표 AGP 버전
- 스파이크로 확인한 사실과 아직 확인하지 못한 가정을 구분해서 적기
- 작업이 끝난 뒤 갱신해야 할 문서(CLAUDE.md의 버전 올리기 안내, 빌드 명령 등)

## 마무리

계획 문서를 다 쓰면 `orca worktree set --worktree active --comment "<한 줄 요약>" --json`으로 워크트리 코멘트를
갱신하고, 사용자에게 검토를 요청한다.
