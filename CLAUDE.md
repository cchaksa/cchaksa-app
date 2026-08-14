# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ChukChukHaksa** (formerly Suwiki) is a Kotlin Multiplatform Compose project of timetable and academic services for Suwon University students. The project uses a single `composeApp` module with Clean Architecture and MVI pattern. Supports Android and iOS with platform-specific widgets.

Native Compose screens cover the timetable feature set (`presentation/timetable`: editor, cell editor,
open-lecture search, semester select, …) plus landing/login and the tab host. Most other surfaces —
including academic info — are served by an embedded web app driven through the WebView bridge.

## Build System and Commands

### Core Build Commands
```bash
# Build entire project (all platforms)
./gradlew build

# Clean build directory
./gradlew clean

# Android-specific builds
./gradlew assembleDebug        # Debug APK
./gradlew assembleRelease      # Release APK
./gradlew installDebug         # Install debug APK to connected device

# iOS Framework builds
./gradlew linkDebugFrameworkIosSimulatorArm64    # iOS Simulator
./gradlew linkReleaseFrameworkIosArm64           # iOS Device
./gradlew embedAndSignAppleFrameworkForXcode     # Xcode integration

# iOS app build (verifies the Swift side too; use the project, not the workspace)
cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp \
  -destination 'generic/platform=iOS Simulator' -configuration Debug build
```

When a change touches both platforms, verify with `./gradlew testDebugUnitTest assembleDebug`,
`./gradlew linkDebugFrameworkIosSimulatorArm64`, and the `xcodebuild` command above.

### Testing Commands
```bash
# Run Android unit tests
./gradlew testDebugUnitTest

# Run Android instrumented tests
./gradlew connectedDebugAndroidTest

# Run all tests
./gradlew check
```

## Architecture

### MVI + Clean Architecture Pattern
- **Presentation Layer**: MVI with custom `MviStore` implementation using StateFlow/Flow
- **Domain Layer**: Use cases with `suspend operator fun invoke()`, returns `Result<T>`
- **Data Layer**: Repository pattern with local (Room/Datastore) and remote (Firebase) data sources

### Key Architectural Components
- **State Management**: Unidirectional Data Flow (UDF) with StateFlow
- **Dependency Injection**: Koin 4.1.0-Beta10 with feature-based modules
- **Navigation**: Jetpack Navigation Compose 2.9.1 with type-safe routes (kotlinx.serialization for argument passing)
- **Database**: Room 2.7.1 with SQLite for local storage (schema in `composeApp/schemas`)
- **Remote Data**: Ktor 3.3.1 REST client (primary) + Firebase via GitLive KMP wrappers 2.1.0 (Remote Config, Realtime Database, Crashlytics, Analytics)
- **WebView**: Much of the product surface is served by an embedded web app. See "WebView Bridge" below.
- **Widgets**: Android (Glance 1.1.1) / iOS (Native WidgetKit) timetable widgets

### WebView Bridge
Several screens (`presentation/webview`, home tab's `WebViewGuideScreen`) host a web app that talks
to native through a **one-way** bridge — the web posts messages, native reacts. There is no reverse
JS-callback channel.

- `WebViewBridgeMessage` (raw message) → `BridgeAction` (`presentation/webview/BridgeAction.kt`) via `toAction(host)`
- Actions: `NavigateWebView`, `NavigateWebViewWithAd`, `RedirectToHome`, `NavigateBack`, `Withdraw`, `ContentRendered`, `Unhandled`
- **Ad gate**: paths listed in `AD_GATED_PATHS` map to `NavigateWebViewWithAd`, which shows a confirm
  dialog → interstitial ad → navigation. Ad load starts on confirm (no preloading). Load/show failure
  still navigates (graceful degradation) with a notice toast.
- Ads go through `common/ad/AdManager` (Android: `AndroidAdManager`; iOS: `IosAdManager` + Swift
  `AdMobBridgeImpl`). It is resolved lazily via `getOrNull<AdManager>()` so a missing binding never crashes the screen.

## Design System

### Dual Theme System
The project uses two parallel design systems:

1. **SuwikiTheme**: Legacy design system with NotoSans fonts
   - 21 text styles (header1-7, body1-7, caption1-7)

2. **CchTheme**: New design system with Paperlogy + SUIT fonts
   - Typography: 13 text styles (titleExlg2, titleExlg, titleLg, bodyExlg, bodyLgStrong, bodyLg, bodyMdStrong, bodyMd, bodySmStrong, bodySm, bodyXsStrong, bodyXs, bodyXxs)
   - Colors: Organized palette with Gray (100-600), Purple (100-600), Red (100-400), Yellow (100-200), Green (100-200)

### Design System Components
appbar, badge, bottomsheet, button, chip, container, dialog, loading, searchbar, tabbar, textfield, toast, webview

### Design System Location
- **Components**: `composeApp/src/commonMain/kotlin/com/chukchukhaksa/mobile/common/designsystem/component/`
- **Themes**: `composeApp/src/commonMain/kotlin/com/chukchukhaksa/mobile/common/designsystem/theme/`
- **Previews**: `composeApp/src/androidMain/kotlin/com/chukchukhaksa/mobile/preview/designsystem/`

## Project Structure

### Multiplatform Targets
- **Android**: Primary target (minSdk 28, targetSdk 36, compileSdk 36)
- **iOS**: Framework generation for iosX64, iosArm64, iosSimulatorArm64. The Xcode side is
  `iosApp/iosApp.xcodeproj` with **Swift Package Manager** dependencies — build/test with
  `-project iosApp.xcodeproj -scheme iosApp`, not the workspace (`iosApp.xcworkspace` is empty and
  `iosApp/Pods/` is a leftover; CocoaPods is not used).

### Key Dependencies
- **Kotlin**: 2.2.21
- **KSP**: 2.2.21-2.0.4
- **AGP**: 8.10.1
- **Compose Multiplatform**: 1.10.0
- **Koin DI**: 4.1.0-Beta10
- **Room Database**: 2.7.1
- **Ktor (HTTP client)**: 3.3.1 (okhttp on Android / darwin on iOS)
- **Firebase**: GitLive KMP 2.1.0 (Android BOM 33.2.0)
- **kotlinx-coroutines**: 1.10.1
- **kotlinx-serialization**: 1.8.1
- **kotlinx-datetime**: 0.7.1
- **kotlinx-immutable**: 0.3.8
- **Datastore Preferences**: 1.1.7
- **KSafe (secure storage)**: 1.6.0
- **Napier (logging)**: 2.7.1
- **Glance (Android Widgets)**: 1.1.1
- **Kakao SDK (login)**: 2.20.6
- **Amplitude (analytics)**: 1.22.4
- **Google Mobile Ads (Android)**: play-services-ads 25.4.0 / iOS uses GoogleMobileAds via SPM

### Package Organization
```
com.chukchukhaksa.mobile/
├── common/
│   ├── ad/                   # Interstitial ad abstraction (AdManager + platform impls)
│   ├── analytics/            # Analytics client (Amplitude / Firebase)
│   ├── designsystem/         # UI components and themes
│   ├── extension/            # Kotlin extensions
│   ├── kmp/                  # KMP platform-specific code
│   ├── model/                # Shared data models
│   ├── provider/             # Platform providers
│   └── ui/                   # Common UI utilities (MviStore)
├── data/                     # Repository implementations
├── di/                       # Dependency injection modules
├── domain/                   # Use cases and repository interfaces
├── local/                    # Local data sources
│   ├── database/             # Room database
│   ├── datasource/           # Local data source implementations
│   └── datastore/            # Datastore preferences
├── presentation/             # Feature screens (MVI pattern)
├── remote/                   # Remote data sources
└── widget/                   # Widget support (expect/actual)
```

## Development Workflow

### Current Project State
- **Main Branch**: `main` (the only branch on the remote)
- **Application ID**: `com.kunize.uswtimetable`
- **Version**: 3.2.1 (versionCode 57 / iOS build 4)
- **Java Compatibility**: Java 17

### Bumping the App Version
Four values, two files — keep them in sync:
- `composeApp/build.gradle.kts`: `versionCode`, `versionName`
- `iosApp/iosApp.xcodeproj/project.pbxproj`: `CURRENT_PROJECT_VERSION`, `MARKETING_VERSION` (**two
  occurrences each** — Debug and Release configs)

`iosApp/Configuration/Config.xcconfig` also defines `MARKETING_VERSION`/`CURRENT_PROJECT_VERSION`, but
those are **dead values** — the target-level settings in `project.pbxproj` win. Edit the pbxproj.

### Secrets and Build-Config IDs
Never hardcode keys or ad unit IDs in source. They live in git-ignored files and flow in through build config:
- **Android**: `local.properties` → `buildConfigField` / `manifestPlaceholders` in `composeApp/build.gradle.kts`
  (`KAKAO_NATIVE_APP_KEY`, `AMPLITUDE_API_KEY_DEV`/`_PROD`, `ADMOB_APP_ID_TEST`/`_PROD`,
  `ADMOB_INTERSTITIAL_AD_UNIT_ID_TEST`/`_PROD`). Debug/release `buildTypes` pick test vs. production IDs.
- **iOS**: `iosApp/Configuration/Config.xcconfig` → `Info.plist` keys, read at runtime via `Bundle.main`.

Missing values fall back to empty strings rather than failing the build, so a blank ad unit ID surfaces
as `AdShowResult.Failed(NotReady)` at runtime, not a compile error.

### MVI Implementation Pattern
Each screen follows this pattern:
```kotlin
// State + SideEffect definitions
data class [Feature]State(...)
sealed interface [Feature]SideEffect

// ViewModel with MviStore delegate
class [Feature]ViewModel(
    private val someUseCase: SomeUseCase
) : ViewModel(), MviStore<State, SideEffect> by mviStore(initialState) {
    fun onAction(action: ...) { ... }
}
```

Use cases use `runCatchingIgnoreCancelled` for error handling.

### Navigation Structure
- Feature-based navigation graphs
- Type-safe route arguments via kotlinx.serialization (JSON encoding to URI)
- Route object pattern with constant routes and parameterized functions
- Nested navigation for complex flows (e.g., timetable → editor → cell editor → lecture search)

### Resource Management
- **Fonts**: Compose Resources in `composeApp/src/commonMain/composeResources/font/`
- **Strings**: Localized resources via Compose Resources
- **Images**: Drawable resources managed through Compose Resources

### Testing Strategy
- **Unit Tests**: Use cases and business logic
- **UI Tests**: Screen-level testing with Compose Test
- **Integration Tests**: Repository and database layer testing

## Development Notes

### Performance Optimizations
- Gradle configuration cache and build cache enabled
- Kotlin daemon: 3GB heap / Gradle JVM: 4GB heap
- Non-transitive R classes enabled
- Core library desugaring enabled for Android

### Code Style
- Kotlin official code style
- Clean Architecture principles
- Single responsibility for use cases
- Immutable state with data classes

### Design System Usage
When creating new UI components:
1. Use `CchTheme` for new features
2. Preview components in `preview/designsystem/` package
3. Follow existing color naming conventions (100-600 scale)
4. Prefer typography styles over hardcoded text styles
