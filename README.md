# ChukChukHaksa (척척학사)

수원대학교 재학생을 위한 시간표 & 강의평가 서비스입니다.
Kotlin Multiplatform (Android + iOS) 기반으로 개발되고 있습니다.

## Tech Stack

| Category | Stack |
|----------|-------|
| Language | Kotlin 2.2, Swift |
| UI | Compose Multiplatform 1.10 |
| Architecture | MVI + Clean Architecture |
| DI | Koin 4.1 |
| Database | Room 2.7 (KMP) |
| Remote | Firebase (GitLive KMP) |
| Widget | Glance (Android), WidgetKit (iOS) |

## Project Structure

단일 `composeApp` 모듈 구조:

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
```

#### iOS

`iosApp/Configuration/Config.xcconfig`에 추가 (gitignore 됨):

```
KAKAO_NATIVE_APP_KEY=노션에서_확인한_키_값
```

### 3. Build

```bash
# Android
./gradlew assembleDebug

# iOS (Simulator)
./gradlew linkDebugFrameworkIosSimulatorArm64
# 이후 Xcode에서 iosApp.xcworkspace 열고 빌드
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
