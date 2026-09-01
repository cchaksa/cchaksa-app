# iOS 시간표 위젯 구현 계획

## 현재 Android 위젯 분석 결과

### Android 위젯 아키텍처:
- **TimetableWidget**: Jetpack Glance 기반 위젯 클래스
- **TimetableWidgetReceiver**: 위젯 업데이트 관리 (매 15-60분)
- **GlanceTimetable**: 시간표 UI 컴포넌트 (시간/수업 컬럼, 셀 구조)
- **데이터 플로우**: GetMainTimetableUseCase → JSON URI 인코딩 → DataStore 저장 → 위젯 표시

### 주요 기능:
- 요일별 시간표 표시 (월~금)
- 이러닝/토요일 수업 별도 표시
- 수업명/교수/강의실 정보 표시
- 자동 업데이트 시스템

## iOS 위젯 구현 계획

### 1. 위젯 확장 프로젝트 생성
- iOS 프로젝트에 Widget Extension 타겟 추가
- Info.plist 및 IntentDefinition 설정

### 2. 핵심 구조 구현
```swift
// TimetableWidget.swift - 메인 위젯
// TimetableTimelineProvider.swift - 데이터 제공자  
// TimetableWidgetView.swift - SwiftUI 뷰
// TimetableEntry.swift - 타임라인 엔트리
```

### 3. 데이터 연동
- KMP의 GetMainTimetableUseCase 연동
- App Groups를 통한 메인 앱-위젯 간 데이터 공유
- iOS의 sendWidgetUpdateCommand 구현

### 4. UI 구현
- Android의 GlanceTimetable과 동일한 레이아웃
- 시간 컬럼 + 요일별 수업 컬럼 구조
- 이러닝/토요일 수업 하단 표시

### 5. 타임라인 관리
- 1시간마다 자동 업데이트 설정
- 메인 앱에서 시간표 변경 시 즉시 업데이트

예상 구현 시간: 3-4시간