# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ChukChukHaksa** (formerly Suwiki) is a Kotlin Multiplatform Compose project for timetable and course evaluation services for Suwon University students. The project has been simplified from a 40+ module architecture to a single `composeApp` module while maintaining Clean Architecture with MVI pattern.

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
- **Domain Layer**: Use cases following single responsibility principle
- **Data Layer**: Repository pattern with Room database and Firebase integration

### Key Architectural Components
- **State Management**: Unidirectional Data Flow (UDF) with StateFlow
- **Dependency Injection**: Koin 4.1.0-Beta10 with feature-based modules
- **Navigation**: Jetpack Navigation Compose with type-safe routes
- **Database**: Room 2.7.1 with SQLite for local storage
- **Remote Data**: Firebase via GitLive KMP wrappers

## Design System

### Dual Theme System
The project uses two parallel design systems:

1. **SuwikiTheme**: Legacy design system with NotoSans fonts
   - 17 text styles (header1-7, body1-7, caption1-7)

2. **CCHaksaTheme**: New design system with Paperlogy + SUIT fonts
   - Typography: 10 text styles (titleExlg2, titleExlg, titleLg, bodyExlg, bodyLgStrong, bodyLg, bodyMdStrong, bodyMd, bodySm, bodyXs)
   - Colors: Organized palette with Gray, Purple, Red, Yellow, Green variants (100-600 naming)

### Design System Location
- **Components**: `composeApp/src/commonMain/kotlin/com/chukchukhaksa/mobile/common/designsystem/component/`
- **Themes**: `composeApp/src/commonMain/kotlin/com/chukchukhaksa/mobile/common/designsystem/theme/`
- **Previews**: `composeApp/src/androidMain/kotlin/com/chukchukhaksa/mobile/preview/designsystem/`

## Project Structure

### Multiplatform Targets
- **Android**: Primary target (minSdk 28, targetSdk 35)
- **iOS**: Framework generation for iOS app (iosX64, iosArm64, iosSimulatorArm64)

### Key Dependencies
- **Compose Multiplatform**: 1.8.1
- **Kotlin**: 2.1.20
- **Koin DI**: 4.1.0-Beta10
- **Room Database**: 2.7.1
- **Firebase**: Via GitLive KMP wrappers

### Package Organization
```
com.chukchukhaksa.mobile/
├── common/
│   ├── designsystem/         # UI components and themes
│   ├── model/               # Shared data models
│   └── util/                # Utility functions
├── feature/                 # Feature modules (MVI screens)
├── data/                    # Repository implementations
├── domain/                  # Use cases and domain models
└── di/                      # Dependency injection modules
```

## Development Workflow

### Current Project State
- **Main Branch**: `develop`
- **Application ID**: `com.kunize.uswtimetable`
- **Version**: 2.3.7 (versionCode 41)

### MVI Implementation Pattern
Each screen follows this contract:
```kotlin
interface [Feature]Contract {
    data class State(...)
    sealed interface SideEffect
}

class [Feature]Store : MviStore<State, SideEffect>
```

### Navigation Structure
- Feature-based navigation graphs
- Type-safe route arguments
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
- Gradle configuration cache enabled
- 4GB heap allocation for builds
- Non-transitive R classes enabled

### Code Style
- Kotlin official code style
- Clean Architecture principles
- Single responsibility for use cases
- Immutable state with data classes

### Design System Usage
When creating new UI components:
1. Use `CCHaksaTheme` for new features
2. Preview components in `preview/designsystem/` package
3. Follow existing color naming conventions (100-600 scale)
4. Prefer typography styles over hardcoded text styles
```