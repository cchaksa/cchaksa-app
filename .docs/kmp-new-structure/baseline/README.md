# 단계 0 기준 산출물

plan.md 단계 0의 3번 기록이다. 이후 단계의 완료 판정은 이 기록과 비교한다.

- 기록일: 2026-10-12
- 기준 커밋: 612794fa (`origin/main`과 같음), 작업 트리 변경 없음
- 버전: AGP 8.10.1, Gradle 8.11.1, Kotlin 2.2.21
- 환경: macOS (Darwin 27.0), JDK 17.0.16(`/opt/homebrew/opt/openjdk@17`), Android build-tools 36.0.0, Xcode 27.0 (27A266a)
- 설정: 워크트리에 있는 실제 설정 파일(`local.properties`, `Config.xcconfig`, Firebase 설정 파일 2개)로 빌드했다

## Android

명령: `./gradlew clean` 뒤 `./gradlew testDebugUnitTest --rerun assembleDebug assembleRelease`

- 결과: `BUILD SUCCESSFUL`. Kotlin 경고(`w:`)와 D8 Kotlin 메타데이터 경고는 0건이다. 로그는 `logs/android-build.log`에 있다.
- 호스트 테스트: 스위트 10개, **테스트 81개**, 실패 0, 오류 0, 건너뜀 0

| 테스트 클래스 | 개수 |
|---|---|
| BridgeActionTest | 15 |
| CheckNeedForceUpdateUseCaseTest | 13 |
| ComposeAppCommonTest | 1 |
| ExchangeWebSessionUseCaseTest | 5 |
| SessionApiImplTest | 6 |
| Sha256Test | 9 |
| TimetableMigrationTest | 10 |
| TokenAuthIntegrationTest | 7 |
| WebViewBridgeMessageTest | 10 |
| WebViewUserAgentTest | 5 |

- 산출물: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`, `composeApp/build/outputs/apk/release/composeApp-release-unsigned.apk`

## APK 스냅샷

`../tools/apk-snapshot.sh`로 만든다. 결과는 `debug/`, `release/`에 있고, 각 디렉터리의 `summary.txt`에 개수를 요약했다.
매니페스트의 Kakao 키와 AdMob 앱 ID는 해시 토큰(`kakaoREDACTED-xxxxxxxx`, `ca-app-pub-REDACTED-xxxxxxxx`)으로 바꿔 저장했다. 같은 값은
같은 토큰이 된다. 에이전트는 `build/` 아래를 읽을 수 없으므로(전역 설정의 `Read(**/build/**)` 거부 규칙) 사용자가 `!` 접두로 직접 실행했다.

| 항목 | debug | release |
|---|---|---|
| `lib/` 항목 | 8 | 8 |
| `libsqliteJni.so` | 없음 | 없음 |
| `assets/composeResources` 파일 | 51 | 51 |
| `assets/` 전체 | 51 | 53 |
| 리소스 이름 | 3,474 | 3,474 |
| 매니페스트 요소 | 156 | 155 |
| badging | `com.kunize.uswtimetable`, versionCode 58, versionName 3.2.2, targetSdk 36 | debug와 같음 |

debug와 release의 차이는 다음뿐이다.

- 매니페스트: debug에만 `android:debuggable="true"`와 `androidx.compose.ui.tooling.PreviewActivity`가 있다. AdMob `APPLICATION_ID` 토큰이
  다르다(debug `a0c532d4`, release `293a45aa`). build type별 테스트·운영 ID 분기가 동작한다는 뜻이다.
- assets: release에만 `assets/dexopt/baseline.prof`, `assets/dexopt/baseline.profm`이 있다.
- `lib/`, 리소스 이름, badging은 같다.

매니페스트 요소 수는 plan.md 3.5절의 값(debug 148, release 147)과 다르다. 같은 커밋과 같은 설정에서 나온 값이므로 세는 방법의 차이로
본다. 이 스크립트는 루트를 포함한 모든 요소를 센다. 이후 단계는 이 스크립트로 얻은 값끼리 비교하고, 판정은 개수보다
`manifest_diff.py`의 결과로 한다.

## iOS

- `./gradlew linkDebugFrameworkIosSimulatorArm64`: 성공(19초). 로그는 `logs/ios-link.log`에 있다.
- `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' -configuration Debug build JAVA_HOME=/opt/homebrew/opt/openjdk@17`:
  `** BUILD SUCCEEDED **`(70초). 로그는 `logs/xcodebuild.log`에 있다.
  - Xcode의 Java Path가 Studio 내장 JBR(JDK 25)이라서 `JAVA_HOME`을 덮어썼다(plan.md 3.5절).
  - "Compile Kotlin Framework" 단계에서 `:composeApp:assembleDebugAppleFrameworkForXcode`가 실행되고,
    `:composeApp:embedAndSignAppleFrameworkForXcode`는 `SKIPPED`로 나온다. generic 시뮬레이터 대상에서는 정상이며, 이후 단계에서도
    같은 양상이면 회귀가 아니다.

## 남기지 않은 것

- APK 파일과 테스트 결과 XML 원본은 복사하지 않았다. 사용자 전역 설정의 `Read(**/build/**)` 거부 규칙 때문이고, APK에는 실제 키가
  들어 있다. 같은 커밋에서 다시 빌드하면 얻을 수 있다.
- `logs/`는 기록한 체크아웃에만 있고 커밋하지 않는다(`.gitignore`). `xcodebuild.log`는 스크립트 단계마다 빌드 설정 전체를 `export`로
  출력하므로 `Config.xcconfig`의 키 값이 들어간다.
