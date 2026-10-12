# ChukChukHaksa (척척학사)

수원대학교 재학생을 위한 시간표 & 강의평가 서비스입니다.
Kotlin Multiplatform (Android + iOS) 기반으로 개발되고 있습니다.

## Tech Stack

| Category | Stack |
|----------|-------|
| Language | Kotlin 2.4, Swift |
| UI | Compose Multiplatform 1.10 |
| Architecture | MVI + Clean Architecture |
| DI | Koin 4.1 |
| Database | Room 2.8 (KMP) |
| Remote | Firebase (GitLive KMP) |
| Widget | Glance (Android), WidgetKit (iOS) |

## Project Structure

Android 앱 모듈 `androidApp`과 공유 KMP 모듈 `shared`로 구성됩니다.

- `androidApp`: Android 진입점(`MainActivity`, `ChukChukHaksaApplication`), 런처 아이콘·테마, 앱 설정(applicationId, 버전, 빌드 타입별 키·광고 ID)
- `shared`: Android·iOS 공유 코드, Android 위젯, iOS 프레임워크(`ComposeApp`)

공유 모듈의 패키지 구조:

```
com.chukchukhaksa.mobile/
├── common/          # 디자인시스템, 확장함수, KMP 플랫폼 코드
├── data/            # Repository 구현
├── di/              # Koin DI 모듈
├── domain/          # UseCase, Repository 인터페이스
├── local/           # Room DB, DataStore
├── presentation/    # 화면 (MVI 패턴)
├── remote/          # Firebase 데이터소스
└── widget/          # 위젯 (Android Glance / iOS WidgetKit)
```

## Setup

### 요구 사항

- Android Studio Quail 4(2026.1.4) 이상. AGP 9.4를 동기화하려면 이 버전이 필요하며, Rabbit 1(2026.2.1)을 권장합니다.
- 명령줄 빌드: JDK 17 이상
- Xcode: Kotlin 프레임워크를 빌드하는 단계에도 JDK 17 이상이 필요합니다. Xcode 설정(Settings → Locations → Custom Paths)에 Java Path를 지정했다면 그 JDK가 쓰이며, Android Studio 내장 JBR을 지정해도 됩니다.

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-org/cchaksa-kmp.git
cd cchaksa-kmp
```

### 2. API Key 설정

키 값은 [Notion](https://www.notion.so/30a1a2480da180408962e123dc1482ca?source=copy_link)에서 확인할 수 있습니다.

#### Android

`local.properties`에 추가 (gitignore 됨):

```properties
KAKAO_NATIVE_APP_KEY=노션에서_확인한_키_값
AMPLITUDE_API_KEY_DEV=노션에서_확인한_키_값
AMPLITUDE_API_KEY_PROD=노션에서_확인한_키_값
ADMOB_APP_ID_TEST=노션에서_확인한_값
ADMOB_APP_ID_PROD=노션에서_확인한_값
ADMOB_INTERSTITIAL_AD_UNIT_ID_TEST=노션에서_확인한_값
ADMOB_INTERSTITIAL_AD_UNIT_ID_PROD=노션에서_확인한_값
```

debug 빌드는 `_TEST`·`_DEV` 값을, release 빌드는 `_PROD` 값을 씁니다. 값이 없으면 빈 문자열로 빌드됩니다.

#### iOS

`iosApp/Configuration/Config.xcconfig`에 추가 (gitignore 됨):

```
KAKAO_NATIVE_APP_KEY=노션에서_확인한_키_값
```

### 3. Firebase 설정 파일

Firebase 콘솔에서 내려받은 설정 파일을 아래 위치에 둡니다. 두 파일 모두 gitignore 대상입니다.

| 플랫폼 | 위치 |
|--------|------|
| Android | `androidApp/` 아래의 `google-services` JSON |
| iOS | `iosApp/iosApp/GoogleService-Info.plist` |

Android 설정 파일이 없으면 google-services 플러그인이 빌드를 실패시킵니다. 예전 체크아웃에서 `composeApp/`에 두었던 파일은 `androidApp/`으로 옮깁니다.

### 4. Build

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS (Simulator)
./gradlew linkDebugFrameworkIosSimulatorArm64
# 이후 Xcode에서 iosApp/iosApp.xcodeproj 열고 빌드
```

## Architecture

MVI + Clean Architecture 패턴을 사용합니다.

```
Presentation (MVI)  →  Domain (UseCase)  →  Data (Repository)
     ↓                                          ↓
  ViewModel + State/SideEffect          Local (Room/DataStore)
                                        Remote (Firebase)
```

각 화면은 `[Feature]State`, `[Feature]SideEffect`, `[Feature]ViewModel` 구조를 따릅니다.

## Logging

로깅은 [Napier](https://github.com/AAkira/Napier)를 사용합니다. 디버깅 시 Logcat(Android) / Xcode 콘솔(iOS)에서 아래 태그로 필터링할 수 있습니다.

### WebView

| Tag | 용도 |
|-----|------|
| `CchWebView` | 웹뷰 컴포저블 — 로드 / 네비게이션 등 생명주기 |
| `WebViewHolder` | 웹뷰 인스턴스 홀더 — 재사용 / 생성 / 해제 |
| `WebViewPreloader` | 웹뷰 프리로드 |
| `WebViewUserAgent` | 웹뷰 User-Agent 구성 |
| `WebConsole` | 웹 페이지의 `console.log` 를 네이티브 로그로 출력 |
| `BridgeAction` | JS ↔ 네이티브 브릿지 액션 |

### Network / Session

| Tag | 용도 |
|-----|------|
| `HttpClient` | HTTP 요청 / 응답 로깅 (디버그 빌드 전용) |
| `ExchangeWebSession` | 웹 세션 쿠키 재교환 |
| `SessionApi` | 세션 API 호출 |
| `UserApi` | 유저 API 호출 |

### App

| Tag | 용도 |
|-----|------|
| `AppLifecycle` | 앱 포그라운드 / 백그라운드 전환 |
| `App` | 앱 전역 (포그라운드 토큰 갱신 등) |
