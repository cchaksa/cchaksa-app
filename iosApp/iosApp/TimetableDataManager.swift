import Foundation
import ComposeApp
import WidgetKit

class TimetableDataManager {
    static let shared = TimetableDataManager()
    private let sharedDefaults = UserDefaults(suiteName: "group.com.kunize.uswtimetable.shared")

    private init() {
        // Listen for timetable data update notifications from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleTimetableDataUpdated),
            name: NSNotification.Name("TimetableDataUpdated"),
            object: nil
        )
    }

    @objc private func handleTimetableDataUpdated() {
        WidgetCenter.shared.reloadTimelines(ofKind: "TimetableWidget")
    }

    func saveTimetableData(_ timetableJson: String) {
        sharedDefaults?.set(timetableJson, forKey: "timetable_data")
        sharedDefaults?.synchronize()
        WidgetCenter.shared.reloadTimelines(ofKind: "TimetableWidget")
    }

    func getTimetableData() -> String? {
        return sharedDefaults?.string(forKey: "timetable_data")
    }

    func clearTimetableData() {
        sharedDefaults?.removeObject(forKey: "timetable_data")
        sharedDefaults?.synchronize()
        WidgetCenter.shared.reloadTimelines(ofKind: "TimetableWidget")
    }

    @objc static func reloadWidget() {
        WidgetCenter.shared.reloadTimelines(ofKind: "TimetableWidget")
    }
}