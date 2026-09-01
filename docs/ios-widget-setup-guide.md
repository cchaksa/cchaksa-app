# iOS 위젯 설정 가이드

## 1. Xcode에서 Widget Extension 추가

### 1.1 Widget Extension Target 생성
1. Xcode에서 `iosApp.xcodeproj` 프로젝트 열기
2. File → New → Target 선택
3. "Widget Extension" 템플릿 선택
4. Product Name: `TimetableWidget`
5. Bundle Identifier: `com.kunize.uswtimetable.TimetableWidget`
6. Language: Swift
7. Include Configuration Intent: ✅ 체크 해제

### 1.2 생성된 기본 파일 교체
생성된 기본 위젯 파일들을 삭제하고, 다음 파일들로 교체:

- `TimetableWidget/TimetableWidget.swift`
- `TimetableWidget/TimetableEntry.swift`
- `TimetableWidget/TimetableTimelineProvider.swift`
- `TimetableWidget/TimetableWidgetView.swift`
- `TimetableWidget/Info.plist`
- `TimetableWidget/TimetableWidget.entitlements`

## 2. App Groups 설정

### 2.1 Apple Developer Console 설정
1. [Apple Developer Console](https://developer.apple.com/account/) 로그인
2. Certificates, Identifiers & Profiles → Identifiers 선택
3. App Groups 생성:
   - Identifier: `group.com.kunize.uswtimetable.shared`
   - Description: "척척학사 위젯 데이터 공유"

### 2.2 메인 앱 App ID에 App Groups 추가
1. 메인 앱 App ID (`com.kunize.uswtimetable`) 선택
2. App Groups capability 추가
3. 생성한 App Group 선택

### 2.3 위젯 App ID 생성 및 설정
1. 새 App ID 생성: `com.kunize.uswtimetable.TimetableWidget`
2. App Groups capability 추가
3. 동일한 App Group 선택

## 3. Xcode 프로젝트 설정

### 3.1 메인 앱 타겟 설정
1. 메인 앱 타겟 선택
2. Signing & Capabilities → + Capability 클릭
3. App Groups 추가
4. `group.com.kunize.uswtimetable.shared` 체크
5. Code Signing Entitlements에 `iosApp.entitlements` 파일 설정

### 3.2 위젯 타겟 설정
1. TimetableWidget 타겟 선택
2. Signing & Capabilities → + Capability 클릭
3. App Groups 추가
4. `group.com.kunize.uswtimetable.shared` 체크
5. Code Signing Entitlements에 `TimetableWidget.entitlements` 파일 설정

### 3.3 위젯 타겟 빌드 설정
1. TimetableWidget 타겟의 Build Settings에서:
   - iOS Deployment Target: 14.0 이상
   - Swift Language Version: 5
2. Build Phases → Link Binary With Libraries에서:
   - ComposeApp.framework 추가
   - WidgetKit.framework 추가 (자동 추가됨)
   - SwiftUI.framework 추가 (자동 추가됨)

## 4. 빌드 및 테스트

### 4.1 빌드 순서
1. 먼저 ComposeApp 프레임워크 빌드:
   ```bash
   ./gradlew linkDebugFrameworkIosSimulatorArm64
   ```

2. Xcode에서 메인 앱과 위젯 빌드:
   - Product → Build (⌘+B)

### 4.2 테스트 방법
1. iOS 시뮬레이터 또는 실기기에서 앱 실행
2. 홈 화면에서 길게 눌러서 편집 모드 진입
3. 왼쪽 상단 + 버튼 클릭
4. "척척학사" 또는 "시간표" 검색
5. 위젯 크기 선택 (Medium 또는 Large)
6. 위젯 추가

## 5. 디버깅

### 5.1 위젯 디버깅
1. Xcode에서 TimetableWidget 타겟 선택
2. Product → Run 또는 ⌘+R
3. 위젯이 시뮬레이터에서 실행됨

### 5.2 로그 확인
- Console.app에서 "TimetableWidget" 필터링하여 로그 확인
- 위젯의 print 문들이 여기에 표시됨

## 6. 배포 시 주의사항

1. App Store Connect에서 위젯 타겟도 함께 업로드 됨
2. 위젯과 메인 앱의 Bundle Identifier가 올바른지 확인
3. App Groups 설정이 프로덕션 환경에서도 동일하게 적용되었는지 확인

## 구현 완료 확인사항

✅ Android 위젯 분석 완료
✅ iOS 위젯 파일 구조 생성 완료
✅ 데이터 공유 메커니즘 구현 완료
✅ UI 컴포넌트 구현 완료 (Android와 동일한 레이아웃)
✅ 타임라인 프로바이더 구현 완료
✅ 실제 시간표 색상 팔레트 적용 완료
✅ App Groups 설정 파일 생성 완료

이제 Xcode에서 위의 설정을 따라하면 iOS에서도 Android와 동일한 시간표 위젯을 사용할 수 있습니다.