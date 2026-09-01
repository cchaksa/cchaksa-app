import Foundation
import AppIntents
import WidgetKit

// iOS 17+ AppIntent to simulate scrolling down by increasing a stored offset
@available(iOS 17.0, *)
struct ScrollDownTimetableIntent: AppIntent {
    static var title: LocalizedStringResource = "다음 시간 보기"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        let suite = "group.com.kunize.uswtimetable.shared"
        let defaults = UserDefaults(suiteName: suite)
        let key = "timetable_scroll_offset"
        let maxKey = "timetable_max_offset"
        let current = defaults?.integer(forKey: key) ?? 0

        // Step by 2 rows
        let step = 2

        // Clamp to the latest known max offset if available; otherwise let view clamp.
        let maxObj = defaults?.object(forKey: maxKey)
        let hasMax = (maxObj != nil)
        let maxOffset = (maxObj as? Int) ?? 0

        let proposed = current + step
        let next = hasMax ? min(proposed, maxOffset) : proposed

        defaults?.set(next, forKey: key)
        defaults?.synchronize()

        // No explicit reload; @AppStorage in the widget view will refresh reactively.
        return .result()
    }
}

@available(iOS 17.0, *)
struct ScrollUpTimetableIntent: AppIntent {
    static var title: LocalizedStringResource = "이전 시간 보기"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        let suite = "group.com.kunize.uswtimetable.shared"
        let defaults = UserDefaults(suiteName: suite)
        let key = "timetable_scroll_offset"
        let current = defaults?.integer(forKey: key) ?? 0
        // Step by 2 rows, clamp to 0
        let step = 2
        let next = max(current - step, 0)
        defaults?.set(next, forKey: key)
        defaults?.synchronize()

        // No explicit reload; @AppStorage in the widget view will refresh reactively.
        return .result()
    }
}

// iOS 17+ AppIntent to force-refresh the widget timeline/data
@available(iOS 17.0, *)
struct RefreshTimetableIntent: AppIntent {
    static var title: LocalizedStringResource = "새로고침"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        let suite = "group.com.kunize.uswtimetable.shared"
        if let defaults = UserDefaults(suiteName: suite) {
            defaults.synchronize()
        }

        WidgetCenter.shared.reloadTimelines(ofKind: "TimetableWidget")
        return .result()
    }
}
