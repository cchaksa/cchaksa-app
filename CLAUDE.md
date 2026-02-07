# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ChukChukHaksa** (formerly Suwiki) is a Kotlin Multiplatform Compose project for timetable and course evaluation services for Suwon University students. The project uses a single `composeApp` module with Clean Architecture and MVI pattern. Supports Android and iOS with platform-specific widgets.

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
```

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
- **Navigation**: Jetpack Navigation Compose 2.9.0-beta02 with type-safe routes (kotlinx.serialization for argument passing)
- **Database**: Room 2.7.1 with SQLite for local storage (schema in `composeApp/schemas`)
- **Remote Data**: Firebase via GitLive KMP wrappers 2.1.0
- **Widgets**: Android (Glance 1.1.1) / iOS (Native WidgetKit) timetable widgets

## Design System

### Dual Theme System
The project uses two parallel design systems:

1. **SuwikiTheme**: Legacy design system with NotoSans fonts
   - 21 text styles (header1-7, body1-7, caption1-7)

2. **CchTheme**: New design system with Paperlogy + SUIT fonts
   - Typography: 13 text styles (titleExlg2, titleExlg, titleLg, bodyExlg, bodyLgStrong, bodyLg, bodyMdStrong, bodyMd, bodySmStrong, bodySm, bodyXsStrong, bodyXs, bodyXxs)
   - Colors: Organized palette with Gray (100-600), Purple (100-600), Red (100-400), Yellow (100-200), Green (100-200)

### Design System Components
appbar, badge, bottomsheet, button, chip, container, dialog, loading, searchbar, tabbar, textfield, toast

### Design System Location
- **Components**: `composeApp/src/commonMain/kotlin/com/chukchukhaksa/mobile/common/designsystem/component/`
- **Themes**: `composeApp/src/commonMain/kotlin/com/chukchukhaksa/mobile/common/designsystem/theme/`
- **Previews**: `composeApp/src/androidMain/kotlin/com/chukchukhaksa/mobile/preview/designsystem/`

## Project Structure

### Multiplatform Targets
- **Android**: Primary target (minSdk 28, targetSdk 35, compileSdk 35)
- **iOS**: Framework generation via `iosApp.xcworkspace` (iosX64, iosArm64, iosSimulatorArm64)

### Key Dependencies
- **Kotlin**: 2.2.21
- **KSP**: 2.2.21-2.0.4
- **Compose Multiplatform**: 1.10.0
- **Koin DI**: 4.1.0-Beta10
- **Room Database**: 2.7.1
- **Firebase**: GitLive KMP 2.1.0 (Android BOM 33.2.0)
- **kotlinx-coroutines**: 1.10.1
- **kotlinx-serialization**: 1.8.1
- **kotlinx-datetime**: 0.7.1
- **kotlinx-immutable**: 0.3.8
- **Datastore Preferences**: 1.1.7
- **Napier (logging)**: 2.7.1
- **Glance (Android Widgets)**: 1.1.1

### Package Organization
```
com.chukchukhaksa.mobile/
├── common/
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
- **Main Branch**: `develop`
- **Application ID**: `com.kunize.uswtimetable`
- **Version**: 3.0.1 (versionCode 51)
- **Java Compatibility**: Java 17

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
